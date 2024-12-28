## 현재 서버는 배포되지 않았습니다.
<br>

## 개요
익명으로 채팅하는 플랫폼 API 서버입니다.
<br>
채팅을 포함한 필수적인 기능들을 구현하는 것에 중점을 두고 점차 확장할 계획입니다.
<br>
단순히 기능 구현에서 멈추지 않고 테스트 속도 및 성능을 비교하여 개선합니다.
<br></br>

## 목표
- 다양한 기능 구현이 아닌 최소한으로 하여 완성도에 초점을 맞춥니다.
- 각 계층의 단위 테스트, 통합 테스트를 작성합니다.
- 특히, DB와 연관된 계층에서 구현할 수 있는 여러 방식들을 테스트 속도 및 성능 측면에서 비교합니다.
<br></br>

## 기술
Kotlin, Spring-boot, Redis, InfluxDB, Docker, Telegraf, Grafana, K6
<br></br>

## 테스트
- Kotlin의 장점을 갖춘 Kotest 프레임워크를 활용하여 테스트 코드 작성
- Testcontainers를 적용하여 애플리케이션 컨텍스트에 기반한 테스트를 간편하게 수행
<br></br>

## 성능 테스트
![image](https://github.com/user-attachments/assets/a23806e4-04cf-44f9-93f1-8ad198c817cf)
<br>
- K6로 서버에 원하는 만큼 트래픽을 발생시켜 성능에 대한 여러 지표 확인
- Telegraf는 Redis 컨테이너에서 원하는 시계열 데이터를 수집하고 InfluxDB에 저장
- InfluxDB에 저장된 데이터를 조회하여 Grafana로 시각화
<br>

