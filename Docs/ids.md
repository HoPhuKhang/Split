# 화면별 id 목록
A(Java)와 B(XML)가 같은 이름을 쓰기 위한 목록.
새 id가 필요하면 여기에 먼저 추가하고 서로 알린다.

## 이름 규칙
- `btn_`: 버튼
- `text_`: 글자
- `edit_`: 입력칸
- `chips_`: 칩 묶음
- `card_`: 카드
- `list_`: 목록을 담는 LinearLayout

| 화면 | 레이아웃 | id | 종류 | 용도 |
| --- | --- | --- | --- | --- |
| 공통 | 모든 activity_*.xml | root | LinearLayout | 가장 바깥 레이아웃, BaseActivity가 상태바 여백 처리 |
| 공통 | view_top_bar | top_bar / btn_back / text_title / text_subtitle | 상단 바 | 뒤로 가기, 제목, 부제 |
| 1 모임 만들기 | activity_main | edit_meal | EditText | 모임 이름 |
| 1 모임 만들기 | activity_main | chips_people | ChipGroup | 참석자 칩 + "사람 추가" 칩 |
| 1 모임 만들기 | activity_main | btn_next | Button | 다음 |
| 2 방식 선택 | activity_mode | card_equal / card_items / card_hoesik | LinearLayout | 정산 방식 3가지 |
| 3a N분의 1 | activity_equal | text_amount | TextView | 총액 |
| 3a N분의 1 | activity_equal | text_each | TextView | ○명 · 1인당 ○원 (2주차 추가) |
| 3a N분의 1 | activity_equal | chips_payer | ChipGroup | 누가 결제했나요? |
| 3a N분의 1 | activity_equal | text_need | TextView | 결제자 안내 (Split.Warn) |
| 3a N분의 1 | activity_equal | numpad | GridLayout | 숫자 키패드 |
| 3a N분의 1 | activity_equal | btn_result | Button | 정산 결과 보기 |
| 3b 차수 입력 | activity_rounds | list_tabs | LinearLayout | 1차 / 2차 / + 차수 탭 |
| 3b 차수 입력 | activity_rounds | chips_attendees | ChipGroup | 이 차에 있었던 사람 |
| 3b 차수 입력 | activity_rounds | chips_payer | ChipGroup | 결제한 사람 (필수) |
| 3b 차수 입력 | activity_rounds | text_need | TextView | 결제자 안내 (Split.Warn) |
| 3b 차수 입력 | activity_rounds | list_items | LinearLayout | 메뉴 줄 목록 |
| 3b 차수 입력 | activity_rounds | text_total | TextView | ○차 합계 |
| 3b 차수 입력 | activity_rounds | btn_add_item | Button | + 메뉴 추가 (결제자 없으면 꺼짐) |
| 3b 차수 입력 | activity_rounds | btn_assign | Button | 먹은 사람 지정하기 |
| 메뉴 한 줄 | item_menu_row | text_name / text_qty / text_price / btn_delete | 한 줄 | 메뉴 이름, x3, 금액, 삭제 |
| 메뉴 추가 시트 | sheet_add_item | chips_quick / edit_item_name / text_price / btn_minus / text_qty / btn_plus / numpad / btn_add | 바텀시트 | 자주 찾는 메뉴, 이름, 가격, 수량, 키패드 |
| 4 먹은 사람 지정 | activity_assign | chips_brush / text_hint / list_rounds / btn_result | | 사람 선택, 안내, 차수별 메뉴, 결과 보기 |
| 메뉴 카드 | item_assign | text_name / text_price / list_who | 카드 | 메뉴 이름, 금액, 먹은 사람 아바타 |
| 5 정산 결과 | activity_result | list_paid / list_transfers / list_people / btn_nudge | | 누가 결제했나요?, 누가 누구에게, 사람별 정리 |
| 6 부드럽게 알리기 | activity_nudge | chips_target / group_tone / btn_send | | 받는 사람, 말투, 보내기 |
| 7 보냈어요 | activity_sent | btn_new | Button | 새 모임 만들기 |