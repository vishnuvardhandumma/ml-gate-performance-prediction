import urllib.request
import json

API = "http://localhost:8080/api/admin/questions"

questions = [
    # ── Computer Science (10) ─────────────────────────────────────────────
    {"subject":"Computer Science","topic":"Data Structures","subtopic":"Stacks","questionText":"Which data structure is used for implementing recursion?","optionA":"Queue","optionB":"Stack","optionC":"Array","optionD":"Linked List","correctOption":"B","marks":1,"difficulty":1},
    {"subject":"Computer Science","topic":"Data Structures","subtopic":"Arrays & Searching","questionText":"What is the time complexity of binary search?","optionA":"O(n)","optionB":"O(n log n)","optionC":"O(log n)","optionD":"O(1)","correctOption":"C","marks":1,"difficulty":1},
    {"subject":"Computer Science","topic":"Operating Systems","subtopic":"Process Scheduling","questionText":"Which scheduling algorithm gives minimum average waiting time?","optionA":"FCFS","optionB":"Round Robin","optionC":"SJF","optionD":"Priority","correctOption":"C","marks":2,"difficulty":2},
    {"subject":"Computer Science","topic":"Operating Systems","subtopic":"Deadlocks","questionText":"What is a deadlock?","optionA":"A process waiting for CPU","optionB":"Two processes waiting for each other indefinitely","optionC":"A process that has completed","optionD":"Thrashing","correctOption":"B","marks":1,"difficulty":1},
    {"subject":"Computer Science","topic":"DBMS","subtopic":"Normalization","questionText":"Which normal form eliminates partial dependencies?","optionA":"1NF","optionB":"2NF","optionC":"3NF","optionD":"BCNF","correctOption":"B","marks":2,"difficulty":2},
    {"subject":"Computer Science","topic":"Computer Networks","subtopic":"OSI Layers","questionText":"Which layer of OSI model handles routing?","optionA":"Data Link","optionB":"Transport","optionC":"Network","optionD":"Physical","correctOption":"C","marks":1,"difficulty":1},
    {"subject":"Computer Science","topic":"Algorithms","subtopic":"Sorting","questionText":"What is the worst-case time complexity of QuickSort?","optionA":"O(n log n)","optionB":"O(n)","optionC":"O(n²)","optionD":"O(log n)","correctOption":"C","marks":2,"difficulty":2},
    {"subject":"Computer Science","topic":"Theory of Computation","subtopic":"Regular Languages","questionText":"Which of the following is NOT a regular language?","optionA":"a*b*","optionB":"(ab)*","optionC":"aⁿbⁿ where n≥1","optionD":"a+b+","correctOption":"C","marks":2,"difficulty":3},
    {"subject":"Computer Science","topic":"Compiler Design","subtopic":"Syntax Analysis","questionText":"Which phase of compiler converts token stream to parse tree?","optionA":"Lexical Analysis","optionB":"Syntax Analysis","optionC":"Semantic Analysis","optionD":"Code Generation","correctOption":"B","marks":1,"difficulty":2},
    {"subject":"Computer Science","topic":"Data Structures","subtopic":"Trees","questionText":"What is the height of a complete binary tree with n nodes?","optionA":"O(n)","optionB":"O(log n)","optionC":"O(n log n)","optionD":"O(√n)","correctOption":"B","marks":2,"difficulty":2},

    # ── Mathematics (10) ──────────────────────────────────────────────────
    {"subject":"Mathematics","topic":"Linear Algebra","subtopic":"Matrix Rank","questionText":"The rank of an n×n identity matrix is:","optionA":"0","optionB":"n-1","optionC":"n","optionD":"1","correctOption":"C","marks":1,"difficulty":1},
    {"subject":"Mathematics","topic":"Linear Algebra","subtopic":"Eigenvalues","questionText":"If eigenvalues of A are 2 and 3, what is det(A)?","optionA":"5","optionB":"6","optionC":"1","optionD":"8","correctOption":"B","marks":2,"difficulty":2},
    {"subject":"Mathematics","topic":"Calculus","subtopic":"Differentiation","questionText":"What is the derivative of e^(2x)?","optionA":"e^(2x)","optionB":"2e^(2x)","optionC":"e^x","optionD":"2e^x","correctOption":"B","marks":1,"difficulty":1},
    {"subject":"Mathematics","topic":"Calculus","subtopic":"Integration","questionText":"The integral of 1/x dx is:","optionA":"x","optionB":"ln|x| + C","optionC":"1/x² + C","optionD":"-1/x² + C","correctOption":"B","marks":1,"difficulty":1},
    {"subject":"Mathematics","topic":"Probability","subtopic":"Basic Probability","questionText":"Two dice are rolled. Probability of getting sum = 7?","optionA":"1/6","optionB":"7/36","optionC":"1/12","optionD":"5/36","correctOption":"A","marks":2,"difficulty":2},
    {"subject":"Mathematics","topic":"Probability","subtopic":"Expectation","questionText":"Expected value of a fair six-sided die is:","optionA":"3","optionB":"3.5","optionC":"4","optionD":"2.5","correctOption":"B","marks":1,"difficulty":1},
    {"subject":"Mathematics","topic":"Discrete Mathematics","subtopic":"Graph Theory","questionText":"How many edges does a complete graph K5 have?","optionA":"5","optionB":"8","optionC":"10","optionD":"12","correctOption":"C","marks":2,"difficulty":2},
    {"subject":"Mathematics","topic":"Discrete Mathematics","subtopic":"Combinatorics","questionText":"What is the value of ⌊log₂ 100⌋?","optionA":"5","optionB":"6","optionC":"7","optionD":"10","correctOption":"B","marks":1,"difficulty":1},
    {"subject":"Mathematics","topic":"Linear Algebra","subtopic":"Matrix Determinants","questionText":"A matrix A is singular if:","optionA":"det(A) = 1","optionB":"det(A) = 0","optionC":"A is identity","optionD":"A is diagonal","correctOption":"B","marks":1,"difficulty":1},
    {"subject":"Mathematics","topic":"Calculus","subtopic":"Limits","questionText":"lim(x→0) sin(x)/x = ?","optionA":"0","optionB":"∞","optionC":"1","optionD":"undefined","correctOption":"C","marks":1,"difficulty":1},

    # ── General Aptitude (10) ─────────────────────────────────────────────
    {"subject":"General Aptitude","topic":"Quantitative Aptitude","subtopic":"Time & Distance","questionText":"If a train travels 360 km in 4 hours, what is its speed in m/s?","optionA":"25","optionB":"90","optionC":"100","optionD":"36","correctOption":"A","marks":1,"difficulty":1},
    {"subject":"General Aptitude","topic":"Quantitative Aptitude","subtopic":"Percentages","questionText":"What is 15% of 200?","optionA":"25","optionB":"30","optionC":"35","optionD":"40","correctOption":"B","marks":1,"difficulty":1},
    {"subject":"General Aptitude","topic":"Logical Reasoning","subtopic":"Blood Relations","questionText":"If A is the mother of B and B is the brother of C, how is A related to C?","optionA":"Sister","optionB":"Aunt","optionC":"Mother","optionD":"Grandmother","correctOption":"C","marks":1,"difficulty":1},
    {"subject":"General Aptitude","topic":"Logical Reasoning","subtopic":"Number Series","questionText":"In a series 2, 6, 12, 20, 30, what is the next number?","optionA":"40","optionB":"42","optionC":"44","optionD":"48","correctOption":"B","marks":1,"difficulty":2},
    {"subject":"General Aptitude","topic":"Verbal Ability","subtopic":"Vocabulary","questionText":"Choose the word closest in meaning to 'Eloquent':","optionA":"Silent","optionB":"Fluent and persuasive","optionC":"Arrogant","optionD":"Confused","correctOption":"B","marks":1,"difficulty":1},
    {"subject":"General Aptitude","topic":"Quantitative Aptitude","subtopic":"Profit & Loss","questionText":"A shopkeeper sells at 20% profit. If CP is ₹500, what is SP?","optionA":"₹550","optionB":"₹580","optionC":"₹600","optionD":"₹620","correctOption":"C","marks":2,"difficulty":1},
    {"subject":"General Aptitude","topic":"Logical Reasoning","subtopic":"Syllogisms","questionText":"All cats are animals. All animals eat food. Therefore:","optionA":"All food is eaten by cats","optionB":"All cats eat food","optionC":"Animals are cats","optionD":"None of the above","correctOption":"B","marks":1,"difficulty":1},
    {"subject":"General Aptitude","topic":"Quantitative Aptitude","subtopic":"Simple Interest","questionText":"Simple interest on ₹1000 at 10% p.a. for 2 years is:","optionA":"₹100","optionB":"₹150","optionC":"₹200","optionD":"₹210","correctOption":"C","marks":1,"difficulty":1},
    {"subject":"General Aptitude","topic":"Verbal Ability","subtopic":"Grammar","questionText":"Identify the grammatically correct sentence:","optionA":"He don't know the answer","optionB":"She doesn't knows the answer","optionC":"They doesn't know the answer","optionD":"He doesn't know the answer","correctOption":"D","marks":1,"difficulty":1},
    {"subject":"General Aptitude","topic":"Quantitative Aptitude","subtopic":"Linear Equations","questionText":"If 3x + 7 = 22, what is x?","optionA":"3","optionB":"4","optionC":"5","optionD":"6","correctOption":"C","marks":1,"difficulty":1},
]

success = 0
failed = 0

for q in questions:
    try:
        data = json.dumps(q).encode('utf-8')
        req = urllib.request.Request(API, data=data, method='POST')
        req.add_header('Content-Type', 'application/json')
        with urllib.request.urlopen(req) as response:
            if response.status == 200:
                success += 1
    except Exception as e:
        print(f"Failed: {q['questionText']} -> {e}")
        failed += 1

print(f"Seed complete. {success} successful, {failed} failed.")
