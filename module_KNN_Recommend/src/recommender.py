import os
import sys
import heapq
import pandas as pd
from collections import defaultdict
from dotenv import load_dotenv

from surprise import Reader, Dataset, KNNBasic

from .database import get_ratings_data, save_recommendations, get_reading_history, get_available_book_ids

if sys.stdout.encoding.lower() != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

load_dotenv()

TOP_N = int(os.getenv("TOP_N", 15))
KNN_K = int(os.getenv("KNN_K", 15))


def run_recommender():
    print("\n>>> BẮT ĐẦU HUẤN LUYỆN VÀ TÍNH TOÁN GỢI Ý KNN <<<", flush=True)

    # 1. Nạp dữ liệu đánh giá từ Database
    df_ratings = get_ratings_data()
    df_ratings['user_id'] = df_ratings['user_id'].astype(int)
    df_ratings['book_id'] = df_ratings['book_id'].astype(int)
    df_ratings['rating'] = df_ratings['rating'].astype(float)

    n_users = df_ratings['user_id'].nunique()
    n_books = df_ratings['book_id'].nunique()
    print(f"[1/4] Đã nạp dữ liệu: {n_users:,} users, {n_books:,} books, {len(df_ratings):,} ratings.", flush=True)

    # 2. Huấn luyện mô hình KNN Cosine bằng Surprise
    print("[2/4] Đang huấn luyện mô hình KNN Cosine...", flush=True)
    reader = Reader(rating_scale=(1, 5))
    data = Dataset.load_from_df(df_ratings[['user_id', 'book_id', 'rating']], reader)
    sim_options = {'name': 'cosine', 'user_based': True}

    trainset = data.build_full_trainset()
    algo = KNNBasic(k=KNN_K, min_k=1, sim_options=sim_options, verbose=False)
    algo.fit(trainset)

    # 3. Lấy danh sách sách khả dụng và lịch sử đọc
    reading_history = get_reading_history()
    candidate_book_ids = [int(b) for b in get_available_book_ids()]

    user_rated_books = defaultdict(set)
    for u_id, b_id in zip(df_ratings['user_id'], df_ratings['book_id']):
        user_rated_books[u_id].add(b_id)

    all_user_ids = [int(trainset.to_raw_uid(i)) for i in trainset.all_users()]
    print(f"[3/4] Đang tính toán gợi ý tự động lần lượt cho {len(all_user_ids):,} Users...", flush=True)

    # 4. Dự đoán điểm số tuần tự từng User
    recommendation_records = []
    for raw_uid in all_user_ids:
        rated_set = user_rated_books.get(raw_uid, set())
        top_n_heap = []

        for book_id in candidate_book_ids:
            if book_id in rated_set or (raw_uid, book_id) in reading_history:
                continue

            pred = algo.predict(raw_uid, book_id)
            score = round(float(pred.est), 4)

            if len(top_n_heap) < TOP_N:
                heapq.heappush(top_n_heap, (score, book_id))
            else:
                if score > top_n_heap[0][0]:
                    heapq.heappushpop(top_n_heap, (score, book_id))

        for score, book_id in sorted(top_n_heap, key=lambda x: x[0], reverse=True):
            recommendation_records.append({'user_id': raw_uid, 'book_id': book_id, 'score': score})

    # 5. Lưu kết quả gợi ý vào Database
    df_recs = pd.DataFrame(recommendation_records)
    print(f"[4/4] Đang lưu {len(df_recs):,} kết quả gợi ý vào Database...", flush=True)
    save_recommendations(df_recs)

    print("=== HOÀN THÀNH DỰ ĐOÁN GỢI Ý ===\n", flush=True)

    return {
        "status": "success",
        "message": f"Recommendations updated. Top {TOP_N} for {len(all_user_ids):,} users."
    }
