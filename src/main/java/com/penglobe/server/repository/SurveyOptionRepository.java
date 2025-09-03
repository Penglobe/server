package com.penglobe.server.repository;

import com.penglobe.server.domain.survey.SurveyOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurveyOptionRepository extends JpaRepository<SurveyOption, Long> {
    //선택지 관리, co2값 조회 등
    //선택지 추가/삭제는 안 할거지만 추후 수정할 걸 생각해서
    @Query("SELECT MAX(o.co2kg) FROM SurveyOption o WHERE o.surveyItem.surveyItemId = :itemId")
    Double findMaxCo2ByItemId(@Param("itemId") Long itemId);

    Optional<SurveyOption> findBySurveyItem_SurveyItemIdAndValue(Long surveyItemId, String value);

    List<SurveyOption> findBySurveyItem_SurveyItemId(Long itemId);
}
