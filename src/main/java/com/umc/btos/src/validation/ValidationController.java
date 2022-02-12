package com.umc.btos.src.validation;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.googleOAuth.OAuthController;
import com.umc.btos.src.validation.model.GetValidationReq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/shops")

public class ValidationController {
    private final OAuthController oAuthController;

    /**
     * @param getValidationReq
     * @param accessToken
     * @return validationController(validationReceipt).java 사용 전에 notion 개발현황>인앱결제>서버>구현 2번을 꼭 읽어주세요.
     */
    @ResponseBody
    @GetMapping("/receipt-validation")
    public BaseResponse<String> validationReceipt(@RequestBody GetValidationReq getValidationReq, String accessToken) throws IOException {

        // ================= Google Credential 생성 =================
        JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        HttpTransport httpTransport = new NetHttpTransport(); //GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);


        // ======================== API 호출 ========================
        AndroidPublisher publisher = new AndroidPublisher.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(getValidationReq.getPackageName())
                .build();

        AndroidPublisher.Purchases.Products.Get get = publisher.purchases().products().get(getValidationReq.getPackageName(), getValidationReq.getProductId(), getValidationReq.getPurchaseToken()); //inapp 아이템의 구매 및 소모 상태 확인
        ProductPurchase productPurchase = get.execute(); //검증 결과
        System.out.println(productPurchase.toPrettyString());

        // 인앱 상품의 소비 상태. 0 아직 소비 안됨(Yet to be consumed) / 1 소비됨(Consumed)
        Integer consumptionState = productPurchase.getConsumptionState();

        // 개발자가 지정한 임의 문자열 정보
        String developerPayload = productPurchase.getDeveloperPayload();

        // 구매 상태. 0 구매완료 / 1 취소됨
        Integer purchaseState = productPurchase.getPurchaseState();

        // 상품이 구매된 시각. 타임스탬프 형태
        Long purchaseTimeMillis = productPurchase.getPurchaseTimeMillis();

        return new BaseResponse<>(productPurchase.toPrettyString());
    }
}
