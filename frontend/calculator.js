let calcDisplay = '0';
let calcEq = '';
let calcMemory = 0;
let trigUnit = 'Deg';

function toggleCalc() {
    const calc = document.getElementById('calcContainer');
    calc.style.display = calc.style.display === 'block' ? 'none' : 'block';
}

function updateCalc() {
    document.getElementById('calcInput').textContent = calcDisplay;
    document.getElementById('calcEq').textContent = calcEq;
}

function setUnit(unit) {
    trigUnit = unit;
}

function calcAction(action) {
    const input = parseFloat(calcDisplay);

    switch(action) {
        case '0': case '1': case '2': case '3': case '4':
        case '5': case '6': case '7': case '8': case '9':
        case '.':
            if (calcDisplay === '0' && action !== '.') calcDisplay = action;
            else if (!calcDisplay.includes('.') || action !== '.') calcDisplay += action;
            break;

        case 'BS':
            calcDisplay = calcDisplay.length > 1 ? calcDisplay.slice(0, -1) : '0';
            break;

        case 'C':
            calcDisplay = '0';
            calcEq = '';
            break;

        // Operators (Standard infix for basic + - * /)
        case '+': case '-': case '*': case '/':
        case '(': case ')':
            calcEq += calcDisplay + ' ' + action + ' ';
            calcDisplay = '0';
            break;

        case '=':
            try {
                let fullMatch = calcEq + calcDisplay;
                // Basic cleanup for eval (not for production, but okay for mock test local env)
                let result = eval(fullMatch.replace(/ /g, ''));
                calcDisplay = result.toString();
                calcEq = '';
            } catch (e) {
                calcDisplay = 'Error';
            }
            break;

        // Value-First Functions
        case 'sin':
            calcDisplay = Math.sin(toRadians(input)).toFixed(4);
            break;
        case 'cos':
            calcDisplay = Math.cos(toRadians(input)).toFixed(4);
            break;
        case 'tan':
            calcDisplay = Math.tan(toRadians(input)).toFixed(4);
            break;
        case 'asin':
            calcDisplay = fromRadians(Math.asin(input)).toFixed(4);
            break;
        case 'acos':
            calcDisplay = fromRadians(Math.acos(input)).toFixed(4);
            break;
        case 'atan':
            calcDisplay = fromRadians(Math.atan(input)).toFixed(4);
            break;
        case 'ln':
            calcDisplay = Math.log(input).toFixed(4);
            break;
        case 'log':
            calcDisplay = Math.log10(input).toFixed(4);
            break;
        case 'exp':
            calcDisplay = Math.exp(input).toFixed(4);
            break;
        case 'sqrt':
            calcDisplay = Math.sqrt(input).toFixed(4);
            break;
        case 'abs':
            calcDisplay = Math.abs(input).toString();
            break;
        case 'inv':
            calcDisplay = (1 / input).toFixed(4);
            break;
        case 'pow2':
            calcDisplay = Math.pow(input, 2).toString();
            break;
        case 'pow3':
            calcDisplay = Math.pow(input, 3).toString();
            break;
        case 'fact':
            calcDisplay = factorial(input).toString();
            break;
        case 'pi':
            calcDisplay = Math.PI.toFixed(6);
            break;
        case 'e':
            calcDisplay = Math.E.toFixed(6);
            break;
        case 'round':
            calcDisplay = Math.round(input).toString();
            break;
        case 'ceil':
            calcDisplay = Math.ceil(input).toString();
            break;
        case 'floor':
            calcDisplay = Math.floor(input).toString();
            break;

        // Memory
        case 'MS':
            calcMemory = input;
            break;
        case 'MR':
            calcDisplay = calcMemory.toString();
            break;
        case 'MC':
            calcMemory = 0;
            break;
        case 'M+':
            calcMemory += input;
            break;
        case 'M-':
            calcMemory -= input;
            break;
    }
    updateCalc();
}

function toRadians(val) {
    return trigUnit === 'Deg' ? val * (Math.PI / 180) : val;
}

function fromRadians(val) {
    return trigUnit === 'Deg' ? val * (180 / Math.PI) : val;
}

function factorial(n) {
    if (n < 0) return NaN;
    if (n === 0) return 1;
    let res = 1;
    for (let i = 2; i <= Math.floor(n); i++) res *= i;
    return res;
}

// Draggable Logic
(function() {
    let pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;
    const header = document.getElementById("calcHeader");
    const container = document.getElementById("calcContainer");

    if (header) {
        header.onmousedown = dragMouseDown;
    }

    function dragMouseDown(e) {
        e = e || window.event;
        e.preventDefault();
        pos3 = e.clientX;
        pos4 = e.clientY;
        document.onmouseup = closeDragElement;
        document.onmousemove = elementDrag;
    }

    function elementDrag(e) {
        e = e || window.event;
        e.preventDefault();
        pos1 = pos3 - e.clientX;
        pos2 = pos4 - e.clientY;
        pos3 = e.clientX;
        pos4 = e.clientY;
        container.style.top = (container.offsetTop - pos2) + "px";
        container.style.left = (container.offsetLeft - pos1) + "px";
        container.style.right = 'auto'; // Disable right anchoring once moved
    }

    function closeDragElement() {
        document.onmouseup = null;
        document.onmousemove = null;
    }
})();
