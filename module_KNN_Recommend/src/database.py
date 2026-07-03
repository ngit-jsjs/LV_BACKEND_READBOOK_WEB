import os
import pandas as pd
from sqlalchemy import create_engine, text
from dotenv import load_dotenv

# Load env variables
load_dotenv()

DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://postgres:123456@localhost:5432/book_system")

engine = create_engine(DATABASE_URL)

def get_ratings_data():
    """
    Lấy dữ liệu ratings từ bảng ratings trong database.
    Trả về: pandas.DataFrame có các cột: user_id, book_id, rating
    """
    query = """
        SELECT r.user_id, r.book_id, r.ratings AS rating 
        FROM ratings r
        WHERE r.ratings > 0
    """
    try:
        with engine.connect() as conn:
            df = pd.read_sql(query, conn)
        print(f"Loaded {len(df)} ratings from database.")
        return df
    except Exception as e:
        print(f"Error loading ratings from database: {e}")
        return pd.DataFrame(columns=['user_id', 'book_id', 'rating'])

def save_recommendations(df_recs):
    """
    Lưu dữ liệu gợi ý (recommendations) vào bảng recommendations.
    df_recs: pandas.DataFrame có các cột: user_id, book_id, score
    """
    if df_recs.empty:
        print("No recommendations to save.")
        return False
    
    try:
        with engine.begin() as conn:
            # Xóa các gợi ý cũ
            conn.execute(text("DELETE FROM recommendations"))
            print("Cleared old recommendations.")
            
            # Ghi đè dữ liệu mới
            df_recs.to_sql(
                name='recommendations',
                con=conn,
                if_exists='append',
                index=False,
                method='multi',
                chunksize=1000
            )
            print(f"Successfully saved {len(df_recs)} new recommendations to database.")
        return True
    except Exception as e:
        err_msg = str(e).split('\n')[0]
        print(f"Error saving recommendations to database: {err_msg}")
        return False

def save_user_similarities(df_sims):
    """
    Lưu độ tương đồng giữa các user vào bảng user_sim.
    df_sims: pandas.DataFrame có các cột: user_id_a, user_id_b, similarity_score
    """
    if df_sims.empty:
        print("No user similarities to save.")
        return False
        
    try:
        with engine.begin() as conn:
            # Xóa các độ tương đồng cũ
            conn.execute(text("DELETE FROM user_sim"))
            print("Cleared old user similarities.")
            
            # Ghi đè dữ liệu mới
            df_sims.to_sql(
                name='user_sim',
                con=conn,
                if_exists='append',
                index=False,
                method='multi',
                chunksize=1000
            )
            print(f"Successfully saved {len(df_sims)} user similarity records to database.")
        return True
    except Exception as e:
        err_msg = str(e).split('\n')[0]
        print(f"Error saving user similarities to database: {err_msg}")
        return False
