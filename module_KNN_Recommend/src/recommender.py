import os
import pandas as pd
from collections import defaultdict
from dotenv import load_dotenv

from surprise import Reader, Dataset, KNNBasic, accuracy
from surprise.model_selection import train_test_split

from .database import get_ratings_data, save_recommendations, get_reading_history, get_available_book_ids

load_dotenv()

TOP_N = int(os.getenv("TOP_N", 15))
KNN_K = int(os.getenv("KNN_K", 15))

def run_recommender():

    # Bước 1: Lấy dữ liệu đánh giá từ database
    df_ratings = get_ratings_data()

    df_ratings['user_id'] = df_ratings['user_id'].astype(int)
    df_ratings['book_id'] = df_ratings['book_id'].astype(int)
    df_ratings['rating'] = df_ratings['rating'].astype(float)

    print(f"Dataset: {df_ratings['user_id'].nunique()} users, {df_ratings['book_id'].nunique()} books, {len(df_ratings)} ratings.")

    # Bước 2: Chuẩn bị dữ liệu cho thư viện Surprise
    reader = Reader(rating_scale=(1, 5))
    data = Dataset.load_from_df(df_ratings[['user_id', 'book_id', 'rating']], reader)
    sim_options = {'name': 'cosine', 'user_based': True}

    # (Đã chuyển phần đánh giá RMSE/MAE sang file evaluate_accuracy.py để tối ưu tốc độ API)
    # Bước 4: Huấn luyện trên toàn bộ dữ liệu
    trainset = data.build_full_trainset()
    algo = KNNBasic(k=KNN_K, min_k=1, sim_options=sim_options, verbose=True)
    algo.fit(trainset)

    # Bước 5: Dự đoán điểm cho các sách mà user chưa đánh giá
    anti_testset = trainset.build_anti_testset()
    predictions = algo.test(anti_testset)
    print(f"Predicted {len(predictions)} (user, book) pairs.")

    # Bước 6: Lọc bỏ sách đã đọc, sách bị ẩn (status != AVAILABLE) và lấy Top-N gợi ý cho mỗi user
    reading_history = get_reading_history()
    available_book_ids = get_available_book_ids()

    user_predictions = defaultdict(list)
    for pred in predictions:
        user_predictions[pred.uid].append((pred.iid, float(pred.est)))

    recommendation_records = []
    for uid, preds in user_predictions.items():
        preds.sort(key=lambda x: x[1], reverse=True)
        count = 0
        for book_id, score in preds:
            # Lọc bỏ sách đã đọc
            if (int(uid), int(book_id)) in reading_history:
                continue
            # Lọc chỉ lấy sách có trạng thái AVAILABLE
            if available_book_ids and int(book_id) not in available_book_ids:
                continue

            recommendation_records.append({
                'user_id': int(uid),
                'book_id': int(book_id),
                'score': round(score, 4)
            })
            count += 1
            if count >= TOP_N:
                break

    # Bước 7: Lưu kết quả gợi ý vào database
    df_recs = pd.DataFrame(recommendation_records)
    success = save_recommendations(df_recs)

    if success:
        return {
            "status": "success",
            "message": f"Recommendations updated. Top {TOP_N} for {len(user_predictions)} users."
        }
    else:
        return {"status": "error", "message": "Failed to save results to database."}
