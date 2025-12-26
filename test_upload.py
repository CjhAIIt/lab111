import requests

token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoic3VwZXJfYWRtaW4iLCJzdWIiOiJzdXBlcl9hZG1pbiIsImlhdCI6MTc2NjM3ODE2OSwiZXhwIjoxNzY2NDY0NTY5fQ.Cm2JP0KbGevfx4BmNRUP-1coaMaAcnTCi-29lc20TqY"
url = "http://localhost:8080/api/file/upload"
file_path = "D:/lab-recruitment/test-image.png"

headers = {
    "Authorization": f"Bearer {token}"
}

with open(file_path, 'rb') as f:
    files = {'file': f}
    response = requests.post(url, headers=headers, files=files)

print("Status Code:", response.status_code)
print("Response:", response.text)