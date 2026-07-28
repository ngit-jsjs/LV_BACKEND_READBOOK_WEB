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

def get_reading_history():
    """
    Lấy danh sách các cặp (user_id, book_id) mà người dùng đã đọc từ bảng reading_history.
    Trả về: set chứa các tuple (user_id, book_id)
    """
    query = "SELECT user_id, book_id FROM reading_history"
    try:
        with engine.connect() as conn:
            df = pd.read_sql(query, conn)
        print(f"Loaded {len(df)} reading history records.")
        return set(zip(df['user_id'].astype(int), df['book_id'].astype(int)))
    except Exception as e:
        print(f"Error loading reading history from database: {e}")
        return set()

def get_available_book_ids():
    """
    Lấy danh sách ID các cuốn sách đang ở trạng thái 'AVAILABLE' từ bảng books.
    Trả về: set chứa các book_id (int)
    """
    query = "SELECT id FROM books WHERE status = 'AVAILABLE'"
    try:
        with engine.connect() as conn:
            df = pd.read_sql(query, conn)
        print(f"Loaded {len(df)} available books.")
        return set(df['id'].astype(int))
    except Exception as e:
        print(f"Error loading available books from database: {e}")
        return set()


