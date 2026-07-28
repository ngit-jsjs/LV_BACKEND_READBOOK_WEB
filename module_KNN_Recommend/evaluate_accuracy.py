import os
import sys
from dotenv import load_dotenv
from surprise import Reader, Dataset, KNNBasic, accuracy
from surprise.model_selection import train_test_split
from src.database import get_ratings_data

# Fix lỗi font tiếng Việt trên màn hình console Windows
if sys.stdout.encoding.lower() != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

load_dotenv()

KNN_K = int(os.getenv("KNN_K", 15))

def evaluate_current_model():
    print(f"=== ĐÁNH GIÁ ĐỘ CHÍNH XÁC MÔ HÌNH (K={KNN_K}) ===")
    
    df_ratings = get_ratings_data()
    if df_ratings.empty:
        print("Không có dữ liệu đánh giá.")
        return

    df_ratings['user_id'] = df_ratings['user_id'].astype(int)
    df_ratings['book_id'] = df_ratings['book_id'].astype(int)
    df_ratings['rating'] = df_ratings['rating'].astype(float)

    reader = Reader(rating_scale=(1, 5))
    data = Dataset.load_from_df(df_ratings[['user_id', 'book_id', 'rating']], reader)
    
    sim_options = {'name': 'cosine', 'user_based': True}

    print("Đang huấn luyện và kiểm tra (80% Train / 20% Test)...")
    train_data, test_data = train_test_split(data, test_size=0.2, random_state=42)
    
    eval_algo = KNNBasic(k=KNN_K, min_k=1, sim_options=sim_options, verbose=True)
    eval_algo.fit(train_data)
    predictions_eval = eval_algo.test(test_data)
    
    rmse = accuracy.rmse(predictions_eval, verbose=False)
    mae = accuracy.mae(predictions_eval, verbose=False)
    
    print(f">> Kết quả Evaluation:")
    print(f"   - RMSE: {rmse:.4f} ")
    print(f"   - MAE : {mae:.4f} ")

if __name__ == "__main__":
    evaluate_current_model()
