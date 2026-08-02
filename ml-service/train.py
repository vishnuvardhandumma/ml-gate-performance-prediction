import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
import joblib
import os

print("Generating training data...")
np.random.seed(42)
n_samples = 2000

math_acc     = np.random.uniform(20, 100, n_samples)
aptitude_acc = np.random.uniform(20, 100, n_samples)
cs_acc       = np.random.uniform(20, 100, n_samples)
time_taken   = np.random.uniform(600, 5400, n_samples)
attempts     = np.random.randint(1, 20, n_samples)

# Realistic GATE scoring — heavily weighted by CS (Standard GATE pattern)
# CS: 70%, Math: 15%, Aptitude: 15%
gate_score = (
    cs_acc       * 0.70 +
    math_acc     * 0.15 +
    aptitude_acc * 0.15 +
    np.random.normal(0, 2, n_samples)
).clip(0, 100)

# Cutoff at 40 marks (realistic GATE cutoff)
cleared = (gate_score >= 40).astype(int)

df = pd.DataFrame({
    "math_accuracy":     math_acc,
    "aptitude_accuracy": aptitude_acc,
    "cs_accuracy":       cs_acc,
    "time_taken":        time_taken,
    "attempts":          attempts,
    "gate_score":        gate_score,
    "cleared_cutoff":    cleared
})

df.to_csv("data/training_data.csv", index=False)
print(f"Training data saved — {n_samples} samples")

features  = ["math_accuracy","aptitude_accuracy",
             "cs_accuracy","time_taken","attempts"]
X         = df[features]
y_score   = df["gate_score"]
y_cleared = df["cleared_cutoff"]

X_train, X_test, y_train, y_test = train_test_split(
    X, y_score, test_size=0.2, random_state=42)

scaler         = StandardScaler()
X_train_scaled = scaler.fit_transform(X_train)
X_test_scaled  = scaler.transform(X_test)

print("Training score model...")
score_model = RandomForestRegressor(
    n_estimators=200, random_state=42, max_depth=10)
score_model.fit(X_train_scaled, y_train)
mae = np.mean(np.abs(score_model.predict(X_test_scaled) - y_test))
print(f"Score model MAE: {mae:.2f}")

print("Training cutoff model...")
X_train2, X_test2, y_train2, y_test2 = train_test_split(
    X, y_cleared, test_size=0.2, random_state=42)
X_train2_scaled = scaler.transform(X_train2)
X_test2_scaled  = scaler.transform(X_test2)

cutoff_model = LogisticRegression(random_state=42, max_iter=500)
cutoff_model.fit(X_train2_scaled, y_train2)
acc = cutoff_model.score(X_test2_scaled, y_test2)
print(f"Cutoff model accuracy: {acc:.2%}")

os.makedirs("models", exist_ok=True)
joblib.dump(score_model,  "models/score_model.pkl")
joblib.dump(cutoff_model, "models/cutoff_model.pkl")
joblib.dump(scaler,       "models/scaler.pkl")
print("All models saved. Training complete.")