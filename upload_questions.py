import urllib.request
import os

filepath = r'C:\Users\chand\OneDrive\Desktop\gate-prediction\gate-prediction\frontend\questions.xlsx'
url = 'http://localhost:8080/api/admin/questions/upload'

boundary = 'boundary123456789'
with open(filepath, 'rb') as f:
    file_data = f.read()

body = (
    ('--' + boundary + '\r\n').encode() +
    b'Content-Disposition: form-data; name="file"; filename="questions.xlsx"\r\n' +
    b'Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n' +
    file_data +
    ('\r\n--' + boundary + '--\r\n').encode()
)

req = urllib.request.Request(url, data=body)
req.add_header('Content-Type', 'multipart/form-data; boundary=' + boundary)
req.add_header('Content-Length', str(len(body)))

try:
    with urllib.request.urlopen(req) as resp:
        print('SUCCESS:', resp.read().decode())
except Exception as e:
    print('ERROR:', e)
