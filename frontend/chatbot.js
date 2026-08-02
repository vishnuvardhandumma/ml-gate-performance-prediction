(function() {

const RESPONSES = {
  greet: {
    patterns: ['hello','hi','hey','good morning','good evening','start'],
    reply: () => `Hello! I'm your GATE preparation assistant. Reply with a number to select an option:\n1️⃣ Syllabus and topics\n2️⃣ Study strategies\n3️⃣ Exam tips\n4️⃣ Subject explanations\n5️⃣ Previous year papers\n\nHow can I help you today?`
  },
  syllabus: {
    patterns: ['syllabus','topics','topic','what to study','portions'],
    reply: () => `GATE CSE syllabus has 3 main areas:\n\n📚 Core Subjects (65 marks)\n• Data Structures & Algorithms\n• Operating Systems\n• DBMS & Computer Networks\n• Theory of Computation\n• Compiler Design\n• Computer Organization\n\n📐 Engineering Mathematics (15 marks)\n• Linear Algebra, Calculus\n• Probability, Discrete Maths\n\n💬 General Aptitude (15 marks)\n• Verbal, Quantitative, Logical`
  },
  study_strategies: {
    patterns: ['study strategy','study strategies','how to prepare','preparation','plan','strategy','study'],
    reply: () => `GATE Preparation Strategy:\n\n📅 12-month plan:\n• Months 1-6: Cover all subjects with standard books\n• Months 7-9: Practice previous year questions\n• Months 10-11: Full mock tests + revision\n• Month 12: Weak area revision + mock tests daily\n\n📚 Recommended books:\n• DS & Algo: Narasimha Karumanchi\n• OS: Galvin\n• DBMS: Navathe\n• Networks: Forouzan\n\n⭐ Most important: Solve at least 3 previous GATE papers every week!`
  },
  exam_tips: {
    patterns: ['exam tip','exam tips','tips','time management','negative marking','trick','hack'],
    reply: () => `Here are the best GATE Exam Tips:\n\n⏱ Time Management:\n• First 90 min: Attempt all easy/medium questions\n• Next 60 min: Tackle hard 2-mark questions\n• Last 30 min: Review marked equations\n\n❌ Negative Marking:\n• Attempt MCQs only if you are confident (avoid blind guessing).\n• MSQ and NAT have NO negative marking — attempt all of them!\n\n🧘‍♂️ Mindset:\n• If a paper is extremely tough, remember it's tough for everyone. Don't panic!`
  },
  subject_explanations: {
    patterns: ['subject explanation','subject explanations','subject','explain subject','explain'],
    reply: () => `I have deep knowledge of all GATE CSE subjects! 🧠\n\nWhich subject would you like me to explain? (Reply with a number)\n1. Data Structures (DS)\n2. Algorithms\n3. Operating Systems (OS)\n4. DBMS\n5. Computer Networks\n6. Theory of Computation (TOC)\n7. Engineering Maths\n8. General Aptitude\n\nOr just type something like "Explain OS"!`
  },
  pyq: {
    patterns: ['previous year','question paper','pyq','past paper','previous paper','old paper','papers'],
    reply: async () => {
      try {
        const res = await fetch(`http://localhost:5000/pyq/branches`);
        const data = await res.json();
        if (data.branches && data.branches.length > 0) {
            currentBranches = data.branches;
            let reply = `I can help you find previous year GATE question papers!\n\nPlease select your **branch** (reply with the number):\n`;
            data.branches.forEach((b, idx) => {
                reply += `${idx + 1}. ${b}\n`;
            });
            return reply;
        } else {
            return `I can help you find previous year GATE question papers!\n\nPlease reply with the **branch** you are looking for (e.g., "CSE" or "Mechanical").`;
        }
      } catch (e) {
        return `I can help you find previous year GATE question papers!\n\nPlease reply with the **branch** you are looking for (e.g., "CSE" or "Mechanical").`;
      }
    }
  },
  ds: {
    patterns: ['data structure','stack','queue','tree','linked list','heap','graph algorithm'],
    reply: () => `Data Structures is one of the highest-weight topics in GATE CSE!\n\nKey topics to master:\n• Arrays and Linked Lists — O(1) vs O(n) operations\n• Stack & Queue — applications, LIFO/FIFO\n• Binary Trees — traversals, BST operations\n• Heaps — min/max heap, heapify\n• Graphs — BFS, DFS, Dijkstra, shortest paths\n\n⭐ Tip: Practice time complexity analysis for every data structure operation.`
  },
  algo: {
    patterns: ['algorithm','sorting','complexity','dynamic programming','greedy','binary search','time complexity'],
    reply: () => `Algorithms carry heavy weightage in GATE!\n\nMust-know algorithms:\n• Sorting: QuickSort O(n log n) avg, MergeSort O(n log n)\n• Searching: Binary Search O(log n)\n• Graph: BFS O(V+E), Dijkstra O(V²)\n• Dynamic Programming: LCS, Knapsack, Matrix Chain\n• Greedy: Kruskal, Prim, Activity Selection\n\n⭐ Tip: For every algorithm, know best/worst/average case complexity.`
  },
  os: {
    patterns: ['operating system','os','process','scheduling','deadlock','memory','virtual memory','paging','semaphore'],
    reply: () => `Operating Systems is asked heavily in GATE!\n\nKey topics:\n• Process Scheduling: FCFS, SJF, Round Robin, Priority\n• Deadlock: 4 conditions, prevention, detection\n• Memory Management: paging, segmentation\n• Virtual Memory: page replacement (LRU, FIFO, Optimal)\n• Synchronization: mutex, semaphore, monitors\n\n⭐ Most common question type: Calculate average waiting time for scheduling algorithms.`
  },
  dbms: {
    patterns: ['dbms','database','sql','normalization','transaction','er model','indexing'],
    reply: () => `DBMS is a must-prepare topic for GATE CSE!\n\nKey areas:\n• ER Model: entities, relationships, cardinality\n• Relational Algebra: select, project, join operations\n• SQL: nested queries, joins, aggregation\n• Normalization: 1NF → 2NF → 3NF → BCNF\n• Transactions: ACID properties, serializability\n• Indexing: B+ trees, hashing\n\n⭐ Tip: Practice SQL queries and normalization problems daily.`
  },
  networks: {
    patterns: ['network','networks','osi','tcp','ip','routing','http','dns','protocol'],
    reply: () => `Computer Networks requires conceptual clarity!\n\nKey topics:\n• OSI Model: 7 layers and their functions\n• TCP/IP: connection establishment, flow control\n• IP Addressing: subnetting, CIDR notation\n• Routing: RIP, OSPF, BGP algorithms\n• Application Layer: HTTP, FTP, DNS, SMTP\n\n⭐ Tip: Draw and memorize the OSI layer functions — guaranteed 1-2 questions every year.`
  },
  toc: {
    patterns: ['automata','toc','theory','turing','regular language','context free','grammar','dfa','nfa'],
    reply: () => `Theory of Computation needs strong practice!\n\nKey topics:\n• Regular Languages: DFA, NFA, Regular Expressions\n• Context-Free Languages: CFG, PDA, ambiguity\n• Decidability: Turing machines, halting problem\n• Closure properties of language classes\n\n⭐ Tip: Practice DFA/NFA conversions and CFG to CNF transformations — very frequently asked.`
  },
  maths: {
    patterns: ['mathematics','maths','linear algebra','calculus','probability','discrete','matrix','eigenvalue','determinant'],
    reply: () => `Engineering Mathematics carries 15 marks in GATE!\n\nHigh priority topics:\n• Linear Algebra: rank, eigenvalues, matrix operations\n• Calculus: limits, derivatives, integration techniques\n• Probability: Bayes theorem, distributions, expectation\n• Discrete Maths: logic, graph theory, combinatorics\n\n⭐ Tip: Don't skip maths — 15 marks from this section can significantly improve your rank.`
  },
  aptitude: {
    patterns: ['aptitude','verbal','reasoning','quant','sentence','vocabulary','ratio','percentage'],
    reply: () => `General Aptitude is 15 marks — easy to score!\n\nAreas to focus:\n• Verbal: synonyms, antonyms, sentence correction\n• Quantitative: percentages, ratios, time-speed-distance\n• Logical: series, analogies, coding-decoding\n\n⭐ Strategy: Practice 30 minutes of aptitude daily. These are guaranteed marks if you prepare consistently.`
  },
  motivate: {
    patterns: ['motivat','discourage','tired','stress','anxiety','give up','difficult','hard','struggling','fear'],
    reply: () => {
      const msgs = [
        `Every GATE topper was once exactly where you are right now. The difference? They didn't stop.\n\n💪 Remember:\n• Consistency beats intensity\n• One topic at a time\n• Your effort today = your rank tomorrow\n\nYou've already started — that's the hardest step. Keep going! 🚀`,
        `Feeling overwhelmed is completely normal. GATE is tough, but so are you!\n\n🌟 Quick reset:\n1. Take a 10-minute break\n2. Review what you've already covered\n3. Pick ONE easy topic and master it today\n\nProgress, no matter how small, is still progress. You've got this! 💯`,
        `Even the students with AIR 1 had days when nothing made sense. What separated them was showing up the next day.\n\n🎯 Today's goal: Just study for 1 hour. That's it.\nTomorrow's goal: Same.\nResult after 6 months: Transformed preparation.\n\nBelieve in the process! 🏆`
      ];
      return msgs[Math.floor(Math.random() * msgs.length)];
    }
  },
  mock: {
    patterns: ['mock test','practice test','test','exam practice'],
    reply: () => `Our mock tests simulate real GATE conditions!\n\n📝 3 Mock Tests available:\n• Each covers CS + Maths + Aptitude\n• 30 questions per test\n• 90-minute timer\n• Negative marking applied\n• Section-wise navigation\n\nAfter each test, our ML model predicts:\n✅ Expected GATE score range\n✅ Probability of clearing cutoff\n✅ Your weak subjects\n\nGo to the Mock Tests page to start!`
  },
  score: {
    patterns: ['score','marks','cutoff','rank','qualify','merit','air'],
    reply: () => `GATE 2025 CSE Score Details:\n\n🎯 Score calculation:\nGATE Score = 350 + (700 × (M - Mq) / (Mt - Mq))\nwhere M = your marks, Mq = qualifying marks, Mt = mean of top 0.1%\n\n📊 General category cutoff: ~25-28/100 (varies yearly)\n\n🏆 For IITs: Usually need 700-800+ GATE score\nFor NITs: Usually need 600-700+ GATE score\n\nUse our prediction system to estimate your score after each mock test!`
  },
  mock: {
    patterns: ['mock test','practice test','test','exam practice'],
    reply: () => `Our mock tests simulate real GATE conditions!\n\n📝 3 Mock Tests available:\n• Each covers CS + Maths + Aptitude\n• 30 questions per test\n• 90-minute timer\n• Negative marking applied\n• Section-wise navigation\n\nAfter each test, our ML model predicts:\n✅ Expected GATE score range\n✅ Probability of clearing cutoff\n✅ Your weak subjects\n\nGo to the Mock Tests page to start!`
  },
  default: {
    reply: (input) => {
      return `I couldn't find a direct answer for "${input}".\n\nI am regularly updated with new GATE content. For now, I can help you with:\n• Syllabus topics (DS, OS, DBMS, etc.)\n• Exam preparation & strategy\n• Mock tests & cutoff scores\n\nTry something like:\n"Explain OS scheduling"\n"How to prepare for GATE?"`;
    }
  }
};

// Fuzzy text matching algorithm
function levenshtein(a, b) {
  if (a.length === 0) return b.length;
  if (b.length === 0) return a.length;
  let matrix = [];
  for (let i = 0; i <= b.length; i++) matrix[i] = [i];
  for (let j = 0; j <= a.length; j++) matrix[0][j] = j;
  for (let i = 1; i <= b.length; i++) {
    for (let j = 1; j <= a.length; j++) {
      if (b.charAt(i - 1) === a.charAt(j - 1)) {
        matrix[i][j] = matrix[i - 1][j - 1];
      } else {
        matrix[i][j] = Math.min(
          matrix[i - 1][j - 1] + 1, // substitution
          Math.min(matrix[i][j - 1] + 1, matrix[i - 1][j] + 1) // insertion/deletion
        );
      }
    }
  }
  return matrix[b.length][a.length];
}

let chatContext = null;
let currentPyqFiles = [];
let currentBranches = [];

async function getResponse(input) {
  const lower = input.toLowerCase().trim().replace(/[?!.,]/g, ''); // Clean input
  const words = lower.split(/\s+/);
  let matchedKey = null;

  // 1. Contextual Selections (Numbers/Direct)
  if (chatContext === 'pyq_select') {
    const numbers = input.match(/\d+/g);
    if (numbers && numbers.length > 0) {
        let reply = `Here are your requested papers!\n\n`;
        let foundAny = false;
        numbers.forEach(numStr => {
            const idx = parseInt(numStr, 10) - 1;
            if (idx >= 0 && idx < currentPyqFiles.length) {
                foundAny = true;
                const f = currentPyqFiles[idx];
                const url = `http://localhost:5000/pyq/download?filename=${encodeURIComponent(f)}`;
                reply += `📄 **${f}**<br><a href="${url}" target="_blank" style="display:inline-block; margin:4px 0 12px 0; padding:6px 12px; background: #38bdf8; color: #0f172a; text-decoration: none; border-radius: 4px; font-weight:bold; font-size:12px;">Download PDF</a><br>\n`;
            }
        });
        if (foundAny) return reply + `\n**What's next?** Type a number or a branch name (e.g., "EC").`;
    }
  }

  // 2. High-Confidence Keyword Match
  for (const [key, data] of Object.entries(RESPONSES)) {
    if (key === 'default' || !data.patterns) continue;
    
    const isHighConfidenceMatch = data.patterns.some(p => {
      const pattern = p.toLowerCase();
      // Match exact phrases or match keywords as whole words
      if (lower === pattern) return true;
      if (pattern.includes(' ')) {
          return lower.includes(pattern); // Match phrase
      }
      return words.includes(pattern); // Match exact word
    });

    if (isHighConfidenceMatch) {
      matchedKey = key;
      break;
    }
  }

  // 3. Subject explanations and menu selection (Numbers)
  if (!matchedKey) {
    if (lower === '1' || lower === 'one') matchedKey = 'syllabus';
    else if (lower === '2' || lower === 'two') matchedKey = 'study_strategies';
    else if (lower === '3' || lower === 'three') matchedKey = 'exam_tips';
    else if (lower === '4' || lower === 'four') matchedKey = 'subject_explanations';
    else if (lower === '5' || lower === 'five') matchedKey = 'pyq';
  }

  if (matchedKey) {
    if (matchedKey === 'subject_explanations') chatContext = 'subjects';
    else if (matchedKey === 'pyq') chatContext = 'pyq';
    else chatContext = null;
    
    const data = RESPONSES[matchedKey];
    return typeof data.reply === 'function' ? data.reply() : data.reply;
  }
  
  // 3. AIS Fallback: If no keywords match, ask the AI brain!
  if (matchedKey) {
    // If they asked for subject explanations or pyq, set context. Otherwise clear it.
    if (matchedKey === 'subject_explanations') chatContext = 'subjects';
    else if (matchedKey === 'pyq') chatContext = 'pyq';
    else chatContext = null;
    
    const data = RESPONSES[matchedKey];
    return typeof data.reply === 'function' ? data.reply() : data.reply;
  }

  // AI Fallback Call
  try {
    const aiRes = await fetch(`http://localhost:8081/api/chat/ask`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query: input })
    });
    
    const aiData = await aiRes.json();
    return aiData.reply || RESPONSES.default.reply(input);
  } catch (e) {
    console.error("AI Chat Error:", e);
    chatContext = null;
    return RESPONSES.default.reply(input);
  }
}

// Build chatbot UI
const style = document.createElement('style');
style.textContent = `
  #gate-chat-btn {
    position: fixed; bottom: 24px; right: 24px; z-index: 999;
    width: 56px; height: 56px; border-radius: 50%;
    background: #38bdf8; border: none; cursor: pointer;
    box-shadow: 0 4px 20px rgba(56,189,248,.4);
    display: flex; align-items: center; justify-content: center;
    font-size: 24px; transition: transform .2s;
  }
  #gate-chat-btn:hover { transform: scale(1.1); }
  #gate-chat-window {
    position: fixed; bottom: 90px; right: 24px; z-index: 998;
    width: 340px; height: 480px;
    background: #1e293b; border: 1px solid #334155;
    border-radius: 16px; display: none;
    flex-direction: column; overflow: hidden;
    box-shadow: 0 8px 32px rgba(0,0,0,.4);
  }
  #gate-chat-window.open { display: flex; }
  .chat-header {
    background: #0f172a; padding: 14px 16px;
    display: flex; align-items: center; gap: 10px;
    border-bottom: 1px solid #334155;
  }
  .chat-avatar {
    width: 34px; height: 34px; border-radius: 50%;
    background: #38bdf8; display: flex; align-items: center;
    justify-content: center; font-size: 16px; color: #0f172a;
    font-weight: 700; flex-shrink: 0;
  }
  .chat-header-info h4 { font-size: 13px; color: #f1f5f9; font-family: 'Segoe UI', sans-serif; }
  .chat-header-info p  { font-size: 11px; color: #22c55e; font-family: 'Segoe UI', sans-serif; }
  .chat-close {
    margin-left: auto; background: none; border: none;
    color: #64748b; cursor: pointer; font-size: 18px;
    padding: 4px; border-radius: 4px; line-height: 1;
  }
  .chat-close:hover { color: #f1f5f9; }
  .chat-messages {
    flex: 1; overflow-y: auto; padding: 14px;
    display: flex; flex-direction: column; gap: 10px;
    scrollbar-width: thin; scrollbar-color: #334155 transparent;
  }
  .msg { max-width: 85%; font-family: 'Segoe UI', sans-serif; font-size: 13px; line-height: 1.5; }
  .msg-bot {
    background: #0f172a; color: #f1f5f9;
    border-radius: 12px 12px 12px 2px; padding: 10px 13px;
    white-space: pre-line; align-self: flex-start;
  }
  .msg-user {
    background: #38bdf8; color: #0f172a;
    border-radius: 12px 12px 2px 12px; padding: 10px 13px;
    align-self: flex-end; font-weight: 500;
  }
  .chat-quick { padding: 8px 12px; display: flex; flex-wrap: wrap; gap: 5px; border-top: 1px solid #334155; }
  .quick-btn {
    background: #0f172a; border: 1px solid #334155;
    border-radius: 12px; padding: 4px 10px;
    color: #94a3b8; font-size: 11px; cursor: pointer;
    font-family: 'Segoe UI', sans-serif; transition: all .2s;
  }
  .quick-btn:hover { border-color: #38bdf8; color: #38bdf8; }
  .chat-input-row {
    padding: 10px 12px; border-top: 1px solid #334155;
    display: flex; gap: 8px;
  }
  .chat-input {
    flex: 1; background: #0f172a; border: 1px solid #334155;
    border-radius: 8px; padding: 9px 12px; color: #f1f5f9;
    font-size: 13px; font-family: 'Segoe UI', sans-serif; outline: none;
  }
  .chat-input:focus { border-color: #38bdf8; }
  .chat-send {
    background: #38bdf8; border: none; border-radius: 8px;
    width: 36px; height: 36px; cursor: pointer;
    display: flex; align-items: center; justify-content: center;
    font-size: 16px; transition: background .2s;
  }
  .chat-send:hover { background: #0ea5e9; }
  .chat-refresh {
    background: #334155; border: none; border-radius: 8px;
    width: 36px; height: 36px; cursor: pointer;
    display: flex; align-items: center; justify-content: center;
    font-size: 18px; transition: background .2s; color: #f1f5f9;
  }
  .chat-refresh:hover { background: #475569; }
  .typing { display: flex; gap: 4px; align-items: center; padding: 4px 0; }
  .typing span {
    width: 6px; height: 6px; border-radius: 50%; background: #475569;
    animation: bounce .8s infinite;
  }
  .typing span:nth-child(2) { animation-delay: .15s; }
  .typing span:nth-child(3) { animation-delay: .3s; }
  @keyframes bounce { 0%,60%,100%{transform:translateY(0)} 30%{transform:translateY(-6px)} }
`;
document.head.appendChild(style);

const btn = document.createElement('button');
btn.id = 'gate-chat-btn';
btn.title = 'GATE Assistant';
btn.innerHTML = '&#129302;';

const win = document.createElement('div');
win.id = 'gate-chat-window';
win.innerHTML = `
  <div class="chat-header">
    <div class="chat-avatar">G</div>
    <div class="chat-header-info">
      <h4>GATE Assistant</h4>
      <p>Online</p>
    </div>
    <button class="chat-close" onclick="toggleChat()">&#10005;</button>
  </div>
  <div class="chat-messages" id="chatMsgs"></div>
  <div class="chat-quick">
    <button class="quick-btn" onclick="quickAsk('Previous year papers')">PYQ Papers</button>
    <button class="quick-btn" onclick="quickAsk('How to prepare for GATE?')">Preparation tips</button>
    <button class="quick-btn" onclick="quickAsk('Explain OS scheduling')">OS Scheduling</button>
    <button class="quick-btn" onclick="quickAsk('What is negative marking?')">Negative marking</button>
    <button class="quick-btn" onclick="quickAsk('I am feeling stressed')">Motivation</button>
  </div>
  <div class="chat-input-row">
    <input class="chat-input" id="chatInput" placeholder="Ask anything about GATE..."
           onkeydown="if(event.key==='Enter') sendMsg()"/>
    <button class="chat-refresh" onclick="clearChat()" title="Reset Chat">&#8635;</button>
    <button class="chat-send" onclick="sendMsg()" title="Send">&#10148;</button>
  </div>
`;

document.body.appendChild(btn);
document.body.appendChild(win);

let chatOpen = false;
let greeted  = false;

btn.onclick = toggleChat;

function toggleChat() {
  chatOpen = !chatOpen;
  win.classList.toggle('open', chatOpen);
  btn.innerHTML = chatOpen ? '&#10005;' : '&#129302;';
  if (chatOpen && !greeted) {
    greeted = true;
    setTimeout(() => addBot(
      `Hello ${localStorage.getItem('name') || 'there'}! 👋\n\nI'm your GATE preparation assistant. Reply with a number to select an option:\n1️⃣ Syllabus and topics\n2️⃣ Study strategies\n3️⃣ Exam tips\n4️⃣ Subject explanations\n5️⃣ Previous year papers\n\nHow can I help you today?`
    ), 300);
  }
}

function addBot(text) {
  const msgs = document.getElementById('chatMsgs');
  const el   = document.createElement('div');
  el.className = 'msg msg-bot';
  el.innerHTML = text.replace(/\n/g, '<br>');
  msgs.appendChild(el);
  msgs.scrollTop = msgs.scrollHeight;
}

function addUser(text) {
  const msgs = document.getElementById('chatMsgs');
  const el   = document.createElement('div');
  el.className = 'msg msg-user';
  el.textContent = text;
  msgs.appendChild(el);
  msgs.scrollTop = msgs.scrollHeight;
}

function showTyping() {
  const msgs = document.getElementById('chatMsgs');
  const el   = document.createElement('div');
  el.className = 'msg msg-bot typing-indicator';
  el.innerHTML = '<div class="typing"><span></span><span></span><span></span></div>';
  msgs.appendChild(el);
  msgs.scrollTop = msgs.scrollHeight;
  return el;
}

function sendMsg() {
  const input = document.getElementById('chatInput');
  const text  = input.value.trim();
  if (!text) return;
  input.value = '';
  addUser(text);
  const t = showTyping();
  setTimeout(async () => {
    t.remove();
    const response = await getResponse(text);
    addBot(response);
  }, 600 + Math.random() * 400);
}

function quickAsk(q) {
  document.getElementById('chatInput').value = q;
  sendMsg();
}

function clearChat() {
  document.getElementById('chatMsgs').innerHTML = '';
  chatContext = null;
  currentPyqFiles = [];
  currentBranches = [];
  addBot(
    `Hello ${localStorage.getItem('name') || 'there'}! 👋\n\nI'm your GATE preparation assistant. Reply with a number to select an option:\n1️⃣ Syllabus and topics\n2️⃣ Study strategies\n3️⃣ Exam tips\n4️⃣ Subject explanations\n5️⃣ Previous year papers\n\nHow can I help you today?`
  );
}

window.toggleChat = toggleChat;
window.sendMsg    = sendMsg;
window.clearChat  = clearChat;
window.quickAsk   = quickAsk;

})();