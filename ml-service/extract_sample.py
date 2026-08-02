import pypdf
import os

pdf_path = os.path.join("pyq_papers", "CS", "CS12024.pdf")
if os.path.exists(pdf_path):
    reader = pypdf.PdfReader(pdf_path)
    # Print the first 2 pages to identify questions
    text = ""
    for i in range(min(3, len(reader.pages))):
        text += f"--- Page {i+1} ---\n"
        text += reader.pages[i].extract_text() + "\n"
    
    with open("extracted_text.txt", "w", encoding="utf-8") as f:
        f.write(text)
    print("Successfully extracted text to extracted_text.txt")
else:
    print(f"File not found: {pdf_path}")
