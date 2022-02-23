package com.umc.btos.src.suggest;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.suggest.model.PostSuggestReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.umc.btos.config.BaseResponseStatus.*;

@RestController
@RequestMapping("/suggests")
public class SuggestController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final SuggestProvider suggestProvider;
    @Autowired
    private final SuggestService suggestService;

    public SuggestController(SuggestProvider suggestProvider, SuggestService suggestService) {
        this.suggestProvider = suggestProvider;
        this.suggestService = suggestService;
    }

    /*
     * 건의 저장
     * [POST] /suggests
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<String> postSuggest(@RequestBody PostSuggestReq postSuggestReq) {
        try {
            int suggestIdx = suggestService.postSuggest(postSuggestReq);

            String result = "건의 저장 완료 (suggestIdx = " + suggestIdx + ")";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
