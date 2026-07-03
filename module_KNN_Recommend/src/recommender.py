import os
import pandas as pd
import numpy as np
from collections import defaultdict
from dotenv import load_dotenv

from surprise import Reader, Dataset, KNNWithMeans

from .database import get_ratings_data, save_recommendations, save_user_similarities

load_dotenv()

# Configurations
TOP_N = int(os.getenv("TOP_N", 15))
MIN_USER_RATINGS = int(os.getenv("MIN_USER_RATINGS", 3))
MIN_BOOK_RATINGS = int(os.getenv("MIN_BOOK_RATINGS", 3))
KNN_K = int(os.getenv("KNN_K", 15))

def run_recommender():
    
    # 1. Load data
    df_ratings = get_ratings_data()
    if df_ratings.empty or len(df_ratings) < 2:
        print("Warning: Ratings data is empty or too small to calculate recommendations.")
        return {"status": "error", "message": "Not enough ratings in database."}

    # Ensure correct data types
    df_ratings['user_id'] = df_ratings['user_id'].astype(int)
    df_ratings['book_id'] = df_ratings['book_id'].astype(int)
    df_ratings['rating'] = df_ratings['rating'].astype(float)
    
    # Filter active users and active books to prevent memory OOM error
    print(f"Original dataset: {df_ratings['user_id'].nunique()} users, {df_ratings['book_id'].nunique()} books, {len(df_ratings)} ratings.")
    
    user_counts = df_ratings['user_id'].value_counts() 
    active_user_ids = set(user_counts[user_counts >= MIN_USER_RATINGS].index)
    df_filtered_users = df_ratings[df_ratings['user_id'].isin(active_user_ids)]
    
    book_counts = df_filtered_users['book_id'].value_counts()
    active_book_ids = set(book_counts[book_counts >= MIN_BOOK_RATINGS].index)
    df_filtered = df_filtered_users[df_filtered_users['book_id'].isin(active_book_ids)]
    
    print(f"Filtered dataset (users >= {MIN_USER_RATINGS} ratings, books >= {MIN_BOOK_RATINGS} ratings):")
    
    if df_filtered.empty or len(df_filtered) < 2:
        print("Warning: Filtered ratings data is empty or too small. Reverting to full dataset.")
        df_filtered = df_ratings

    reader = Reader(rating_scale=(1, 10))
    data = Dataset.load_from_df(df_filtered[['user_id', 'book_id', 'rating']], reader)
    trainset = data.build_full_trainset()
    
    print(f"Training dataset built with: {trainset.n_users} users, {trainset.n_items} books.")

    # 2. Configure KNN algorithm
    sim_options = {
        'name': 'cosine',
        'user_based': True
    }
    
    # KNNWithMeans handles mean subtraction normalization automatically
    algo = KNNWithMeans(k=KNN_K, min_k=1, sim_options=sim_options, verbose=True)
    
    print("Training KNN model (User-based CF)...")
    algo.fit(trainset)
    print("KNN model training complete.")

    # 3. Extract and save user similarity scores (only for active users who are in trainset)
    if len(df_filtered) > 2:
        print("Extracting user similarity matrix...")
        sim_matrix = algo.sim
        user_sim_records = []
        num_users = trainset.n_users
        
        # Save similarities for nearest neighbors to prevent DB bloat
        for i in range(num_users):
            raw_uid_a = trainset.to_raw_uid(i)
            neighbors = algo.get_neighbors(i, k=KNN_K)
            for neighbor_inner_id in neighbors:
                raw_uid_b = trainset.to_raw_uid(neighbor_inner_id)
                sim_score = float(sim_matrix[i, neighbor_inner_id])
                
                if np.isnan(sim_score) or sim_score <= 0:
                    continue
                    
                user_sim_records.append({
                    'user_id_a': int(raw_uid_a),
                    'user_id_b': int(raw_uid_b),
                    'similarity_score': round(sim_score, 4)
                })
        
        df_sims = pd.DataFrame(user_sim_records)
        if not df_sims.empty:
            save_user_similarities(df_sims)
        else:
            print("No significant user similarities found.")
    else:
        print("Skipping user_sim saving (insufficient data).")

    # 4. Generate Top-N recommendations for active users
    print(f"Generating Top {TOP_N} recommendations for active users...")
    
    active_user_ids = df_filtered['user_id'].unique()
    testset_pairs = []
    
    for raw_uid in active_user_ids:
        inner_uid = trainset.to_inner_uid(raw_uid)
        user_candidates = set()
        
        user_rated_inner_ids = {item_id for (item_id, _) in trainset.ur[inner_uid]}
        candidate_inner_item_ids = set()
        
        neighbors = algo.get_neighbors(inner_uid, k=KNN_K)
        for neighbor_inner_id in neighbors:
            for (item_id, _) in trainset.ur[neighbor_inner_id]:
                if item_id not in user_rated_inner_ids:
                    candidate_inner_item_ids.add(item_id)
                        
        for inner_iid in candidate_inner_item_ids:
            user_candidates.add(trainset.to_raw_iid(inner_iid))
            
        # Add to batch testset
        for raw_iid in user_candidates:
            testset_pairs.append((raw_uid, raw_iid, 0))
            
    # Run batch prediction via Cython-optimized Surprise test()
    predictions = algo.test(testset_pairs)
    print("Batch prediction complete.")
    
    # Group predictions by user
    user_predictions = defaultdict(list)
    for pred in predictions:
        user_predictions[pred.uid].append((pred.iid, float(pred.est)))
        
    # Build recommendation records
    recommendation_records = []
    for raw_uid, preds in user_predictions.items():
        preds.sort(key=lambda x: x[1], reverse=True)
        top_predictions = preds[:TOP_N]
        
        for raw_iid, score in top_predictions:
            recommendation_records.append({
                'user_id': int(raw_uid),
                'book_id': int(raw_iid),
                'score': round(score, 4)
            })
            
    df_recs = pd.DataFrame(recommendation_records)
    success = save_recommendations(df_recs)
    if success:
        return {
            "status": "success",
            "message": f"Successfully updated recommendations (Top {TOP_N}) and similarities."
        }
    else:
        return {
            "status": "error",
            "message": "Failed to save results to database."
        }
