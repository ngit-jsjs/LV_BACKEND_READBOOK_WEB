from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from src.recommender import run_recommender

app = FastAPI(
    title="Book Recommendation API (KNN Collaborative Filtering)",
    description="Module gợi ý sách sử dụng thuật toán KNN (User-based Collaborative Filtering) và thư viện Surprise.",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
def read_root():
    return {
        "message": "Welcome to Book Recommendation Module",
        "algorithm": "KNN Collaborative Filtering",
        "library": "scikit-surprise",
        "documentation": "/docs"
    }

@app.post("/recommend/train")
def train_recommender():
    try:
        result = run_recommender()
        if result.get("status") == "success":
            return result
        else:
            raise HTTPException(status_code=500, detail=result.get("message"))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))



if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
