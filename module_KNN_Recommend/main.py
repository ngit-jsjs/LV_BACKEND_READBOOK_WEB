import uvicorn
from fastapi import FastAPI
from src.recommender import run_recommender

app = FastAPI(title="KNN Recommender System Service")


@app.post("/recommend/train")
def train_recommender():
    print("\n[API EVENT] Đã nhận được Request kích hoạt /recommend/train từ Client/Web!", flush=True)
    result = run_recommender()
    return result


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
