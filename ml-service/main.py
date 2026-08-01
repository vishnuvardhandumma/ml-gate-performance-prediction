import os
import uvicorn

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from pydantic import BaseModel

app = FastAPI(title="GATE ML Prediction Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

try:
    from predict import predict_performance
    models_loaded = True
    print("ML models loaded successfully")
except ImportError as e:
    models_loaded = False
    print(f"Warning: Models not loaded — {e}")


ENGINEERING_MATHEMATICS = "Engineering Mathematics"
GENERAL_APTITUDE = "General Aptitude"


class PredictRequest(BaseModel):
    subject_scores: dict
    time_taken_secs: int = 1800
    total_attempts: int = 1


class StudyPlanRequest(BaseModel):
    weak_subjects: list[str]
    weak_topics_detailed: list[dict] = []
    days_to_exam: int = 90
    hours_per_day: float = 4.0
    total_attempts: int = 1

@app.get("/health")
def health():
    return {"status": "running", "models_loaded": True}

@app.get("/pyq/practice/{subject}")
def get_pyq_practice(subject: str, topic: str | None = None):
    import json

    from ai_practice_engine import generate_questions
    bank_path = os.path.join(os.path.dirname(__file__), "pyq_bank.json")
    questions = []
    
    # Subject Mapping / Aliases
    subject_map = {
        "Probability": ENGINEERING_MATHEMATICS,
        "Linear Algebra": ENGINEERING_MATHEMATICS,
        "Calculus": ENGINEERING_MATHEMATICS,
        "Discrete Mathematics": ENGINEERING_MATHEMATICS,
        "Quantitative Aptitude": GENERAL_APTITUDE,
        "Verbal Ability": GENERAL_APTITUDE,
        "Logical Reasoning": GENERAL_APTITUDE,
    }
    
    target_subject = subject_map.get(subject, subject)
    
    # 1. Try to load from specialized PYQ Bank (Paper-based)
    if os.path.exists(bank_path):
        with open(bank_path, 'r') as f:
            bank = json.load(f)
            questions = bank.get(target_subject, [])
    
    # 2. Dynamic Topic-Specific AI Practice (LLM-Simulated)
    # If the user requested a specific topic (e.g., "Deadlocks")
    if topic:
        ai_questions = generate_questions(target_subject, topic)
        if ai_questions:
            # Boost: Prepend AI questions or replace
            questions = ai_questions + questions

    # 3. Hybrid Fallback
    if not questions:
        return {
            "questions": [], 
            "status": "not_found", 
            "message": "AI is generating simulated practice for " + (topic if topic else subject)
        }
    
    return {"questions": questions, "status": "ok", "mode": "AI-Enhanced"}

@app.get("/pyq/branches")
def list_branches():
    pyq_dir = os.path.join(os.path.dirname(__file__), "pyq_papers")
    if not os.path.exists(pyq_dir):
        return {"branches": []}
    
    branches = [d for d in os.listdir(pyq_dir) 
                if os.path.isdir(os.path.join(pyq_dir, d))]
    return {"branches": sorted(branches)}

@app.get("/pyq/search")
def search_pyq(branch: str):
    pyq_dir = os.path.join(os.path.dirname(__file__), "pyq_papers")
    if not os.path.exists(pyq_dir):
        return {"files": []}

    branch_upper = branch.upper()
    branch_lower = branch.lower()
    
    # Map common input names to folder names
    branch_map = {
        "cse": "CS",
        "computer science": "CS",
        "ece": "EC",
        "electronics": "EC",
        "electrical": "EE",
        "mechanical": "ME",
        "civil": "CE",
        "instrumentation": "IN",
        "chemical": "CH"
    }
    
    target_folder = branch_map.get(branch_lower, branch_upper)
    search_path = os.path.join(pyq_dir, target_folder)
    
    matched = []
    
    # If a direct folder exists, list its contents
    if os.path.exists(search_path) and os.path.isdir(search_path):
        for f in os.listdir(search_path):
            if f.lower().endswith(".pdf"):
                matched.append(f)
    else:
        # Fallback to global search if folder not found
        search_prefixes = [branch_lower]
        # Reverse mapping search prefixes for the fallback
        for k, v in branch_map.items():
            if branch_lower == k:
                search_prefixes.append(v.lower())
                
        for root, dirs, files in os.walk(pyq_dir):
            for f in files:
                f_lower = f.lower()
                if f_lower.endswith(".pdf"):
                    for prefix in search_prefixes:
                        if prefix in f_lower:
                            matched.append(f)
                            break
                    
    return {"files": sorted(matched)}

@app.get("/pyq/download")
def download_pyq(filename: str):
    pyq_dir = os.path.join(os.path.dirname(__file__), "pyq_papers")
    if os.path.exists(pyq_dir):
        for root, dirs, files in os.walk(pyq_dir):
            if filename in files:
                filepath = os.path.join(root, filename)
                return FileResponse(filepath, media_type="application/pdf", filename=filename)
    raise HTTPException(status_code=404, detail="File not found")

@app.post("/predict")
def predict(request: PredictRequest):
    if not models_loaded:
        raise HTTPException(status_code=503,
            detail="Models not loaded. Run train.py first.")
    try:
        return predict_performance(request.model_dump())
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/study-plan")
def study_plan(request: StudyPlanRequest):
    try:
        return generate_study_plan(
            request.weak_subjects,
            request.weak_topics_detailed or [],
            request.days_to_exam,
            request.hours_per_day,
            request.total_attempts
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def generate_study_plan(weak_subjects, weak_topics_detailed, days_to_exam,
                        hours_per_day, total_attempts):

    # Per-topic AI overcome plans
    TOPIC_OVERCOME_PLANS = {
        "Data Structures": {
            "key_concepts": ["Arrays & Strings", "Linked Lists", "Stacks & Queues", "Trees & BST", "Heaps", "Graphs", "Hash Tables"],
            "approach": "Start with linear structures, then move to non-linear. Use the 📚 icon to solve GATE PYQs immediately after watching a video to bridge the application gap.",
            "daily_practice": "Solve 5 problems daily on LeetCode/GFG starting from easy. Focus on pattern recognition.",
            "resources": ["CLRS Book Ch.10-14", "GeeksforGeeks DS", "Visualgo.net for visualization"],
            "tips": "Tutorials often skip edge cases — solve the linked 📚 PYQs to see how GATE tests these concepts."
        },
        "Algorithms": {
            "key_concepts": ["Sorting & Searching", "Divide & Conquer", "Dynamic Programming", "Greedy", "Graph Algorithms", "Complexity Analysis"],
            "approach": "Master time and space complexity first. Use the 📚 icon to bridge tutorials with actual exam-level difficulty.",
            "daily_practice": "Pick one algorithm family per day. Solve its GATE PYQs and generalise the pattern.",
            "resources": ["CLRS Book", "Abdul Bari YouTube", "GATE PYQ 2010-2024"],
            "tips": "Recurrence relations and Master Theorem are high-value — use the 📚 link to master GATE-style trick questions."
        },
        "Operating Systems": {
            "key_concepts": ["Process Scheduling", "Memory Management", "Deadlocks", "File Systems", "Synchronization", "Page Replacement"],
            "approach": "Study scheduling with numericals. Videos cover theory, but the 📚 link will show you the complex numericals GATE actually asks.",
            "daily_practice": "Solve one scheduling / page replacement numerical daily.",
            "resources": ["Galvin OS Book", "Neso Academy YouTube", "GFG OS topic-wise"],
            "tips": "Deadlocks and Banker's algorithm are numerical-heavy — don't just watch videos, solve the 📚 PYQs."
        },
        "DBMS": {
            "key_concepts": ["ER Diagrams", "Normalization", "SQL Queries", "Transactions & ACID", "Indexing", "Relational Algebra"],
            "approach": "Understand normalization (1NF to BCNF) conceptually then practice decomposition. SQL nested queries appear often.",
            "daily_practice": "Write 3 SQL queries daily. Practice Relational Algebra translation.",
            "resources": ["Korth DBMS Book", "Sanchit Jain YouTube", "GATE DBMS PYQs"],
            "tips": "Functional dependency and closure calculation are guaranteed GATE topics — practise 20+ problems."
        },
        "Computer Networks": {
            "key_concepts": ["OSI & TCP/IP Layers", "Routing Algorithms", "Subnetting", "TCP/UDP", "Congestion Control", "MAC Protocols"],
            "approach": "Learn each layer protocol with its header fields. Subnetting numericals are a guaranteed 2-mark question.",
            "daily_practice": "Solve one subnetting problem and one routing problem daily.",
            "resources": ["Forouzan CN Book", "Gate Smashers YouTube", "GFG CN"],
            "tips": "Know sliding window calculations, CRC, and Hamming code for error detection thoroughly."
        },
        "Theory of Computation": {
            "key_concepts": ["DFA/NFA", "Regular Expressions", "Context-Free Grammars", "Pushdown Automata", "Turing Machines", "Decidability"],
            "approach": "Build automata from scratch for each language class. Pumping lemma proofs need intensive practice.",
            "daily_practice": "Construct one DFA/NFA and one CFG daily from a given language description.",
            "resources": ["Ullman TOC Book", "Ravindrababu Y. YouTube", "GFG TOC"],
            "tips": "Closure properties of regular and CFL languages appear every year — make a comparison table."
        },
        "Compiler Design": {
            "key_concepts": ["Lexical Analysis", "Parsing (LL, LR)", "Syntax Directed Translation", "Symbol Table", "Code Optimization", "Code Generation"],
            "approach": "Focus on parsing: LL(1) and LR(0)/SLR/CLR/LALR parsing tables. These dominate GATE marks.",
            "daily_practice": "Construct one parsing table daily. Trace through 2 grammar examples.",
            "resources": ["Dragon Book (Aho et al.)", "Easy Engineering YouTube", "GFG Compiler Design"],
            "tips": "FIRST and FOLLOW set calculation is the foundation — master it before attempting parsing tables."
        },
        "Linear Algebra": {
            "key_concepts": ["Matrix Operations", "Rank & Nullity", "Eigenvalues & Eigenvectors", "Linear Transformations", "Systems of Equations"],
            "approach": "Practice Gaussian elimination and eigenvalue calculation until they are mechanical. Focus on rank-nullity theorem numericals.",
            "daily_practice": "Solve 3 linear algebra numericals from GATE PYQs daily.",
            "resources": ["Gilbert Strang Linear Algebra", "3Blue1Brown YouTube", "GATE Maths PYQs"],
            "tips": "Eigenvalues of special matrices (symmetric, identity, diagonal) are shortcut goldmines."
        },
        "Calculus": {
            "key_concepts": ["Limits & Continuity", "Differentiation", "Integration", "Maxima & Minima", "Taylor Series", "Partial Derivatives"],
            "approach": "Revise standard derivatives and integrals formulae. GATE Calculus focuses on application — not proofs.",
            "daily_practice": "Solve 5 calculus numericals daily covering differentiation and integration.",
            "resources": ["NCERT Maths 12", "Khan Academy Calculus", "GATE Maths Handbooks"],
            "tips": "L'Hopital's rule, integration by parts, and definite integral properties appear repeatedly."
        },
        "Probability": {
            "key_concepts": ["Basic Probability", "Conditional Probability", "Bayes' Theorem", "Random Variables", "Distributions", "Expectation"],
            "approach": "Build intuition with counting problems then move to distributions. Bayes' theorem derivations from scratch.",
            "daily_practice": "Solve 3 probability problems covering expectation and distributions daily.",
            "resources": ["Sheldon Ross Probability", "MIT OpenCourseWare Probability", "GFG Probability"],
            "tips": "Expected value, variance formulas, and Poisson/Binomial distributions are frequently tested."
        },
        "Discrete Mathematics": {
            "key_concepts": ["Sets & Relations", "Graph Theory", "Combinatorics", "Propositional Logic", "Recurrence Relations", "Lattices"],
            "approach": "Study propositional logic first (truth tables, tautologies), then graph theory (Hamiltonian, Eulerian, colouring).",
            "daily_practice": "Solve one combinatorics and one graph theory problem daily.",
            "resources": ["Rosen Discrete Maths", "Kenneth Rosen Book", "GFG Discrete Math"],
            "tips": "Pigeonhole principle, inclusion-exclusion, and Hasse diagrams are comparatively easy marks — never skip."
        },
        "Quantitative Aptitude": {
            "key_concepts": ["Percentages", "Ratio & Proportion", "Time & Work", "Speed & Distance", "Profit & Loss", "Number Systems"],
            "approach": "Build speed with shortcut formulas. GA section is purely time-management — aim to finish in 20 minutes.",
            "daily_practice": "Solve 10 aptitude MCQs under timed conditions (2 minutes each) daily.",
            "resources": ["R.S. Aggarwal Quantitative Aptitude", "Indiabix Aptitude", "GATE GA PYQs"],
            "tips": "Learn percentage-fraction equivalents, LCM/HCF tricks, and time-speed-distance shortcuts."
        },
        "Logical Reasoning": {
            "key_concepts": ["Syllogisms", "Number Series", "Coding-Decoding", "Blood Relations", "Direction Sense", "Analogies"],
            "approach": "Practice pattern recognition. Each type has a fixed solving strategy — memorise the approach, not the answer.",
            "daily_practice": "Solve 10 logical reasoning questions daily mixing different types.",
            "resources": ["R.S. Aggarwal Logical Reasoning", "Indiabix Reasoning", "GATE GA PYQs"],
            "tips": "Venn diagrams for syllogisms and systematic elimination for series problems are the two most powerful techniques."
        },
        "Verbal Ability": {
            "key_concepts": ["Reading Comprehension", "Sentence Completion", "Grammar", "Vocabulary", "Para Jumbles"],
            "approach": "Read one English article daily. Focus on sentence structure and common error types tested in GATE GA.",
            "daily_practice": "Complete one reading comprehension passage and 5 vocabulary questions daily.",
            "resources": ["Wren & Martin Grammar", "Word Power Made Easy", "GATE GA PYQs"],
            "tips": "Subject-verb agreement, tense errors, and preposition usage are the most common grammar traps in GATE."
        },
    }

    # All GATE CSE topics with base hours needed
    ALL_TOPICS = {
        "Data Structures":      {"hours": 20, "subject": "Computer Science", "priority": 1},
        "Algorithms":           {"hours": 18, "subject": "Computer Science", "priority": 1},
        "Operating Systems":    {"hours": 16, "subject": "Computer Science", "priority": 1},
        "DBMS":                 {"hours": 14, "subject": "Computer Science", "priority": 1},
        "Computer Networks":    {"hours": 14, "subject": "Computer Science", "priority": 1},
        "Theory of Computation":{"hours": 12, "subject": "Computer Science", "priority": 2},
        "Compiler Design":      {"hours": 10, "subject": "Computer Science", "priority": 2},
        "Linear Algebra":       {"hours": 10, "subject": "Mathematics",      "priority": 1},
        "Calculus":             {"hours": 8,  "subject": "Mathematics",      "priority": 1},
        "Probability":          {"hours": 8,  "subject": "Mathematics",      "priority": 1},
        "Discrete Mathematics": {"hours": 8,  "subject": "Mathematics",      "priority": 2},
        "Quantitative Aptitude":{"hours": 6,  "subject": "General Aptitude", "priority": 1},
        "Logical Reasoning":    {"hours": 6,  "subject": "General Aptitude", "priority": 1},
        "Verbal Ability":       {"hours": 4,  "subject": "General Aptitude", "priority": 2},
    }

    # Boost hours for weak subjects (subject-level)
    for topic, info in ALL_TOPICS.items():
        for ws in weak_subjects:
            if ws.lower() in topic.lower() or \
               info["subject"].lower() in ws.lower():
                info["hours"] = int(info["hours"] * 1.5)
                info["priority"] = 1

    # Further boost for specific weak topics (topic-level — more precise)
    for topic, info in ALL_TOPICS.items():
        for wt_obj in weak_topics_detailed:
            parent_subj = wt_obj.get("subject", "")
            if parent_subj.lower() in topic.lower() or topic.lower() in parent_subj.lower():
                info["hours"] = int(info["hours"] * 1.8)  # heavier boost for exact weak topic
                info["priority"] = 1
                info["is_weak"] = True

    # Build topic overcome plans for specifically weak topics
    topic_overcome_plans = []
    matched_topics = set()
    for wt_obj in weak_topics_detailed:
        parent_subj = wt_obj.get("subject", "")
        subtopic = wt_obj.get("subtopic", "")
        for topic_name, plan in TOPIC_OVERCOME_PLANS.items():
            if parent_subj.lower() in topic_name.lower() or topic_name.lower() in parent_subj.lower():
                unique_key = f"{parent_subj}_{subtopic}"
                if unique_key not in matched_topics:
                    topic_overcome_plans.append({
                        "topic": subtopic,
                        "subject": topic_name,
                        "hours_allocated": ALL_TOPICS.get(topic_name, {}).get("hours", 10),
                        "key_concepts": plan["key_concepts"],
                        "approach": plan["approach"],
                        "daily_practice": plan["daily_practice"],
                        "resources": plan["resources"],
                        "tips": plan["tips"]
                    })
                    matched_topics.add(unique_key)

    # Sort by priority then hours
    sorted_topics = sorted(
        ALL_TOPICS.items(),
        key=lambda x: (x[1]["priority"], -x[1]["hours"])
    )

    # Build weekly plan
    weeks      = max(1, days_to_exam // 7)
    hours_pw   = hours_per_day * 7
    weekly_plan = []

    topic_idx = 0
    for week in range(1, min(weeks + 1, 9)):
        week_topics  = []
        hours_left   = hours_pw
        week_focus   = []

        while hours_left > 0 and topic_idx < len(sorted_topics):
            topic_name, info = sorted_topics[topic_idx]
            alloc = min(info["hours"], hours_left)
            if alloc > 0:
                week_topics.append({
                    "topic":   topic_name,
                    "subject": info["subject"],
                    "hours":   round(alloc, 1),
                    "is_weak": any(
                        ws.lower() in topic_name.lower() or
                        info["subject"].lower() in ws.lower()
                        for ws in weak_subjects
                    )
                })
                week_focus.append(topic_name)
                hours_left -= alloc
            topic_idx += 1
            if topic_idx >= len(sorted_topics):
                break

        if not week_topics:
            # Revision week
            week_topics = [{"topic": "Full Revision",
                            "subject": "All Subjects",
                            "hours": round(hours_pw, 1),
                            "is_weak": False}]
            week_focus = ["Revision + Mock Tests"]

        motivation = get_week_motivation(week)

        weekly_plan.append({
            "week":       week,
            "title":      f"Week {week}",
            "topics":     week_topics,
            "focus":      ", ".join(week_focus[:3]),
            "total_hours": round(sum(t["hours"] for t in week_topics), 1),
            "motivation": motivation
        })

    # Overall recommendation
    phase = "Beginner"
    if total_attempts >= 5:   phase = "Advanced"
    elif total_attempts >= 2: phase = "Intermediate"

    return {
        "total_weeks":            len(weekly_plan),
        "days_to_exam":           days_to_exam,
        "hours_per_day":          hours_per_day,
        "weak_subjects":          weak_subjects,
        "weak_topics":            weak_topics_detailed,
        "phase":                  phase,
        "weekly_plan":            weekly_plan,
        "overall_advice":         get_overall_advice(
                                      weak_subjects, days_to_exam),
        "daily_schedule":         get_daily_schedule(
                                      hours_per_day, weak_subjects),
        "topic_overcome_plans":   topic_overcome_plans
    }


def get_week_motivation(week):
    msgs = [
        "Great start! Building your foundation this week.",
        "You're making progress. Every hour counts!",
        "Halfway through — you're stronger than you think.",
        "Stay consistent. Your rank is being built right now.",
        "Push through — the finish line is getting closer!",
        "Almost there. Make every study session count.",
        "Final stretch. Review, revise, and believe in yourself.",
        "You've put in the work. Now sharpen your skills!"
    ]
    return msgs[min(week - 1, len(msgs) - 1)]


def get_overall_advice(weak_subjects, days_to_exam):
    if days_to_exam < 30:
        return ("Less than 30 days to exam! Focus only on high-weight "
                "topics and solve previous year papers daily.")
    if days_to_exam < 60:
        return ("60 days is enough for a strong comeback. "
                "Dedicate 70% time to weak subjects and 30% to revision.")
    if len(weak_subjects) >= 3:
        return ("Multiple weak areas detected. "
                "Tackle one subject at a time. "
                "Start with the highest-weight topic.")
    return ("You're on track! Maintain consistency, "
            "take mock tests weekly, and review mistakes carefully.")


def get_daily_schedule(hours_per_day, weak_subjects):
    schedule = []
    remaining = hours_per_day

    # Weak subject first
    if weak_subjects and remaining > 0:
        alloc = min(remaining, hours_per_day * 0.5)
        schedule.append({
            "time_slot": "Morning",
            "activity":  f"Weak subject focus: {weak_subjects[0]}",
            "hours":     round(alloc, 1),
            "tip":       "Study weak topics when your mind is freshest"
        })
        remaining -= alloc

    # Core subject
    if remaining > 0:
        alloc = min(remaining, hours_per_day * 0.35)
        schedule.append({
            "time_slot": "Afternoon",
            "activity":  "Core subject practice + problem solving",
            "hours":     round(alloc, 1),
            "tip":       "Solve at least 20 MCQs after every topic"
        })
        remaining -= alloc

    # Revision
    if remaining > 0:
        schedule.append({
            "time_slot": "Evening",
            "activity":  "Previous year questions + mock test review",
            "hours":     round(remaining, 1),
            "tip":       "Never end a session without reviewing mistakes"
        })

    return schedule


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=int(os.environ.get("PORT", 5000)), reload=False)