package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import roomescape.IntegrationTestSupport;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.UserReservationResponse;

class ReservationControllerTest extends IntegrationTestSupport {

    String createdId;
    int adminReservationSize;
    int userReservationSize;

    @DisplayName("어드민의 예약 CRUD")
    @TestFactory
    Stream<DynamicTest> dynamicAdminTestsFromCollection() {
        return Stream.of(
                dynamicTest("예약을 목록을 조회한다.", () -> {
                    adminReservationSize = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().get("/admin/reservations")
                            .then().log().all()
                            .statusCode(200).extract()
                            .response().jsonPath().getList("$").size();
                }),
                dynamicTest("예약을 추가한다.", () -> {
                    Map<String, Object> params = Map.of(
                            "memberId", 1L,
                            "date", "2025-10-06",
                            "timeId", 1L,
                            "themeId", 1L);

                    createdId = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .body(params)
                            .when().post("/admin/reservations")
                            .then().log().all()
                            .statusCode(201).extract().header("location").split("/")[2];
                }),
                dynamicTest("존재하지 않는 시간으로 예약을 추가할 수 없다.", () -> {
                    Map<String, Object> params = Map.of(
                            "memberId", 1L,
                            "date", "2025-10-05",
                            "timeId", 100L,
                            "themeId", 1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .body(params)
                            .when().post("/admin/reservations")
                            .then().log().all()
                            .statusCode(400);
                }),
                dynamicTest("존재하지 않는 테마로 예약을 추가할 수 없다.", () -> {
                    Map<String, Object> params = Map.of(
                            "memberId", 1L,
                            "date", "2025-10-05",
                            "timeId", 1L,
                            "themeId", 100L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .body(params)
                            .when().post("/admin/reservations")
                            .then().log().all()
                            .statusCode(400);
                }),
                dynamicTest("존재하지 않는 회원으로 예약을 추가할 수 없다.", () -> {
                    Map<String, Object> params = Map.of(
                            "memberId", 100L,
                            "date", "2025-10-05",
                            "timeId", 1L,
                            "themeId", 1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .body(params)
                            .when().post("/admin/reservations")
                            .then().log().all()
                            .statusCode(400);
                }),
                dynamicTest("예약 목록을 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().get("/admin/reservations")
                            .then().log().all()
                            .statusCode(200).body("size()", is(adminReservationSize + 1));
                }),
                dynamicTest("예약을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().delete("/admin/reservations/" + createdId)
                            .then().log().all()
                            .statusCode(204);
                }),
                dynamicTest("이미 삭제된 예약을 삭제시도하면 statusCode가 400이다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().delete("/admin/reservations/" + createdId)
                            .then().log().all()
                            .statusCode(400);
                })
        );
    }

    @DisplayName("유저의 예약 생성")
    @TestFactory
    Stream<DynamicTest> dynamicUserTestsFromCollection() {
        return Stream.of(
                dynamicTest("내 예약 목록을 조회한다.", () -> {
                    userReservationSize = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .when().get("/reservations-mine")
                            .then().log().all()
                            .statusCode(200).extract()
                            .response().jsonPath().getList("$").size();
                }),
                dynamicTest("예약을 추가한다.", () -> {
                    Map<String, Object> params = Map.of(
                            "date", LocalDate.now(),
                            "timeId", 1L,
                            "themeId", 1L);

                    createdId = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .body(params)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201).extract().header("location").split("/")[2];
                }),
                dynamicTest("내 예약 목록을 조회하면 사이즈가 1증가한다.", () -> {
                    UserReservationResponse userReservationResponse = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .when().get("/reservations-mine")
                            .then().log().all()
                            .statusCode(200)
                            .body("size()", is(userReservationSize + 1))
                            .extract().as(UserReservationResponse[].class)[0];

                    assertAll(
                            () -> assertThat(userReservationResponse.theme()).isEqualTo("이름1"),
                            () -> assertThat(userReservationResponse.date()).isEqualTo(LocalDate.now()),
                            () -> assertThat(userReservationResponse.time()).isEqualTo(LocalTime.of(9, 0, 0)),
                            () -> assertThat(userReservationResponse.status()).isEqualTo(ReservationStatus.RESERVED.name())
                    );
                }),
                dynamicTest("존재하지 않는 시간으로 예약을 추가할 수 없다.", () -> {
                    Map<String, Object> params = Map.of(
                            "date", "2025-10-05",
                            "timeId", 100L,
                            "themeId", 1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .body(params)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(400);
                }),
                dynamicTest("존재하지 않는 테마로 예약을 추가할 수 없다.", () -> {
                    Map<String, Object> params = Map.of(
                            "memberId", 1L,
                            "date", "2025-10-05",
                            "timeId", 1L,
                            "themeId", 100L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .body(params)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(400);
                }),
                dynamicTest("유저는 예약을 삭제할 수 없다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .when().delete("/admin/reservations/" + createdId)
                            .then().log().all()
                            .statusCode(404);
                }),
                dynamicTest("어드민은 예약을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().delete("/admin/reservations/" + createdId)
                            .then().log().all()
                            .statusCode(204);
                })
        );
    }
}
