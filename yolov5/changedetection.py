import os
import cv2
import pathlib
import requests
from datetime import datetime

class ChangeDetection:
    result_prev = []
    HOST = 'http://127.0.0.1:8000'
    username = 'admin'
    password = 'admin'
    token = 'ce93d899dfa4118dd90cef0e35002451e85ab1b1'
    title = ''
    text = ''

    def __init__(self, names):
        self.result_prev = [0 for i in range(len(names))]
        res = requests.post(self.HOST + '/api-token-auth/', {
            'username': self.username,
            'password': self.password,
        })
        res.raise_for_status()
        self.token = res.json()['token']  # 토큰 저장
        print(self.token)

    def add(self, names, detected_current, save_dir, image):
        self.title = ''
        self.text = ''
        change_flag = 0  # 변화 감지 플래그

        # names가 딕셔너리일 경우 리스트로 변환
        if isinstance(names, dict):
            names = list(names.values())

        print("Detected Current:", detected_current)
        print("Names:", names)

        for i, name in enumerate(names):
            if name in ['dog', 'person']:
                if self.result_prev[i] == 0 and detected_current[i] == 1:
                    change_flag = 1
                    self.title = name
                    self.text += name + ", "

        self.result_prev = detected_current[:]  # 객체 검출 상태 저장
        if change_flag == 1:
            self.send(save_dir, image)

    def send(self, save_dir, image):
        now = datetime.now()
        today = datetime.now()
        save_path = pathlib.Path(os.getcwd()) / save_dir / 'detected' / str(today.year) / str(today.month) / str(today.day)
        print("Save Path:", save_path)
        pathlib.Path(save_path).mkdir(parents=True, exist_ok=True)

        full_path = save_path / '{0}-{1}-{2}-{3}.jpg'.format(today.hour, today.minute, today.second, today.microsecond)

        dst = cv2.resize(image, dsize=(320, 240), interpolation=cv2.INTER_AREA)
        cv2.imwrite(str(full_path), dst)

        headers = {'Authorization': 'JWT ' + self.token, 'Accept': 'application/json'}
        data = {
            'author': 1,
            'title': self.title,
            'text': self.text,
            'created_date': now,
            'published_date': now,
        }
        file = {'image': open(full_path, 'rb')}
        res = requests.post(self.HOST + '/api_root/Post/', data=data, files=file, headers=headers)
        print("Response Status Code:", res.status_code)
        print("Response Content:", res.content)

        if res.status_code == 201:
            print("Successfully uploaded to the blog.")
        else:
            print(f"Failed to upload: {res.status_code}, {res.text}")
