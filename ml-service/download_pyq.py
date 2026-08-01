import os
import sys

try:
    import gdown
except ImportError:
    print("Installing gdown...")
    os.system(f"{sys.executable} -m pip install gdown")
    import gdown

folder_url = 'https://drive.google.com/drive/folders/1sV6FgtOUDl_PGjc36Zdc0eJwK1zZ_2OF'
output_dir = 'pyq_papers'

if not os.path.exists(output_dir):
    os.makedirs(output_dir)

print(f"Downloading Google Drive folder to {output_dir}...")
gdown.download_folder(url=folder_url, output=output_dir, quiet=False, remaining_ok=True)

print("\n--- Download Complete ---")
print("Files downloaded:")
for root, dirs, files in os.walk(output_dir):
    for f in files:
        print(os.path.join(root, f))
