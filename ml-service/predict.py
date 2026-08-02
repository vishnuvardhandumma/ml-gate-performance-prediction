import numpy as np
import joblib

score_model  = joblib.load("models/score_model.pkl")
cutoff_model = joblib.load("models/cutoff_model.pkl")
scaler       = joblib.load("models/scaler.pkl")

def get_subject_accuracy(subject_scores: dict, subject: str) -> float:
    return subject_scores.get(subject, 50.0)

def predict_performance(data: dict) -> dict:
    subject_scores = data.get("subject_scores", {})
    time_taken     = data.get("time_taken_secs", 1800)
    attempts       = data.get("total_attempts", 1)

    math_acc     = get_subject_accuracy(subject_scores, "Mathematics")
    aptitude_acc = get_subject_accuracy(subject_scores, "General Aptitude")
    cs_acc       = get_subject_accuracy(subject_scores, "Computer Science")

    features = np.array([[
        math_acc, aptitude_acc, cs_acc, time_taken, attempts
    ]])
    features_scaled = scaler.transform(features)

    predicted_score = float(score_model.predict(features_scaled)[0])
    predicted_score = round(max(0, min(100, predicted_score)), 2)
    score_min       = round(max(0,   predicted_score - 5), 2)
    score_max       = round(min(100, predicted_score + 5), 2)

    cutoff_prob = float(
        cutoff_model.predict_proba(features_scaled)[0][1]) * 100
    
    # Consistency Check: If score is near/above 25 (cutoff), 
    # ensure probability isn't literally 0%
    if predicted_score >= 25 and cutoff_prob < 5:
        cutoff_prob = round(max(5.0, (predicted_score - 20) * 2), 2)
    elif predicted_score >= 20 and cutoff_prob < 1:
        cutoff_prob = 1.0 # Minimum sliver of hope
    
    cutoff_prob = round(cutoff_prob, 2)

    # ============================================================
    # WEAK SUBJECT DETECTION - ENHANCED VERSION
    # ============================================================
    # Changed threshold from 50 to 60 because:
    # - User's 53.3% accuracy should be flagged as weak
    # - 60% is a more realistic threshold for identifying areas needing improvement
    # - This captures mistakes even when overall score is decent
    # ============================================================
    weak_subjects = []
    subject_map = {
        "Mathematics":      math_acc,
        "General Aptitude": aptitude_acc,
        "Computer Science": cs_acc
    }
    for subject, acc in subject_map.items():
        # FIXED: Changed threshold from < 50 to < 60
        # This will now detect weak areas where accuracy is below 60%
        if acc < 60:
            # Calculate severity for better messaging
            if acc < 40:
                severity = "Critical"
                advice = f"Critical weakness in {subject}! Immediate practice needed."
            elif acc < 50:
                severity = "High"
                advice = f"Low accuracy in {subject}. Priority: Practice fundamental concepts."
            else:  # 50-60
                severity = "Medium"
                advice = f"Room for improvement in {subject}. Focus on weak topics."
            
            weak_subjects.append({
                "subject":  subject,
                "accuracy": round(acc, 2),
                "message":  advice,
                "severity": severity
            })

    return {
        "predicted_score_min": score_min,
        "predicted_score_max": score_max,
        "predicted_score":     predicted_score,
        "cutoff_probability":  cutoff_prob,
        "weak_subjects":       weak_subjects,
        "total_attempts":      attempts,
        "recommendation":      get_recommendation(cutoff_prob)
    }

def get_recommendation(cutoff_prob: float) -> str:
    if cutoff_prob >= 75:
        return "Excellent! You are well prepared. Keep practicing."
    elif cutoff_prob >= 50:
        return "Good progress. Focus on weak subjects to improve."
    elif cutoff_prob >= 25:
        return "Needs improvement. Increase daily practice hours."
    else:
        return "Critical. Start with basics and take more mock tests."