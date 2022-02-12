package com.umc.btos.src.validation;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.validation.model.GetValidationReq;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

@RestController
@RequestMapping("/shops")
public class Payment {

    /**
     * 영수증 검증 서버
     * [GET] /shops/receipt-validation
     *
     * @param getValidationReq
     * @return String
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws GoogleJsonResponseException
     *
     * Payment.java 사용 전에 notion 개발현황>인앱결제>서버>구현 1번을 꼭 읽어주세요.
     * 다른 부분 테스트를 위해서 실행 할 때는 아래 에러나는 부분을 지우지 말고 주석처리 해주세요! 저 부분은 서비스 계정이 발급되면 채울 수 있습니다!
     */
    @ResponseBody
    @GetMapping("/receipt-validation")
    public BaseResponse<String> validationReceipt(@RequestBody GetValidationReq getValidationReq) throws IOException, GeneralSecurityException, GoogleJsonResponseException {
        //TODO 1. GoogleCredential 생성
        //     2. API 호출
        try {

            // ==================== GoogleCredential 생성 ====================
            //credential 생성 전 필요한 변수 선언
            String emailAddress = "themusic025@gmail.com"; // 서비스 계정을 생성하면서 발급받은 email address //임시로 에러 안나게하려고 채워넣음, emailAddress를 yml에 넣어둬야할까..?
            JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
            HttpTransport httpTransport = new NetHttpTransport(); //GoogleNetHttpTransport.newTrustedTransport();

            //본격적인 GoogleCredential 생성, (json이든 p12이든 application.yml에 저장해서 @Value로 받아오는게 좋을듯)
            // P12 비밀키 파일 방식
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setServiceAccountId(emailAddress)
                    .setServiceAccountPrivateKeyFromP12File(new File("P12File이위치한경로"))
                    .setServiceAccountScopes(Collections.singleton("https://www.googleapis.com/auth/androidpublisher"))
                    .build();

            // JSON 비밀키 파일 방식
            InputStream jsonInputStream = ResourceUtils.getURL(); //InputStream 타입으로 json 파일 저장, ()안에 json 파일이 위치한 URL 입력

            GoogleCredential googleCredential = GoogleCredential.fromStream(jsonInputStream, httpTransport, JSON_FACTORY)
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/androidpublisher"))
                    .createDelegated(emailAddress); //build 할 때는 문제없음


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
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}
