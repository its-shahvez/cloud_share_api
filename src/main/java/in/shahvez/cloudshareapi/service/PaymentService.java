package in.shahvez.cloudshareapi.service;


import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import in.shahvez.cloudshareapi.document.PaymentTransaction;
import in.shahvez.cloudshareapi.document.ProfileDocument;
import in.shahvez.cloudshareapi.dto.PaymentDTO;
import in.shahvez.cloudshareapi.dto.PaymentVerificationDTO;
import in.shahvez.cloudshareapi.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Formatter;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ProfileService profileService;
    private final UserCreditService userCreditService;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public PaymentDTO createOrder(PaymentDTO paymentDTO){
        try {
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            String clerkId = currentProfile.getClerkId();
            RazorpayClient razorpayClient =  new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", paymentDTO.getAmount());
            orderRequest.put("currency", paymentDTO.getCurrency());
            orderRequest.put("receipt", "order_"+System.currentTimeMillis());

          Order order = razorpayClient.orders.create(orderRequest);
          String orderId = order.get("id");

          //create pending transaction record
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .clerkId(clerkId)
                    .orderId(orderId)
                    .planId(paymentDTO.getPlanId())
                    .amount(paymentDTO.getAmount())
                    .currency(paymentDTO.getCurrency())
                    .status("PENDING")
                    .transactionDate(LocalDateTime.now())
                    .userEmail(currentProfile.getEmail())
                    .userName(currentProfile.getFirstName()+" "+currentProfile.getLastName())
                    .build();
            paymentTransactionRepository.save(transaction);

            return PaymentDTO.builder()
                    .orderId(orderId)
                    .success(true)
                    .message("Order create successfully")
                    .build();



        } catch (Exception e){
           return PaymentDTO.builder()
                   .success(false)
                   .message("Error Creating order: "+e.getMessage())
                   .build();
        }

    }
    public PaymentDTO verifyPayment(PaymentVerificationDTO request){
        try{
        ProfileDocument currentProfile = profileService.getCurrentProfile();
        String clerkid =currentProfile.getClerkId();


       String data = request.getRazorpay_order_id() + "|" +request.getRazorpay_payment_id();

        String generatedSignature = generateHmachSha256Signature(data, razorpayKeySecret);

        if(!generatedSignature.equals(request.getRazorpay_signature())){
            updateTransactionStatus(request.getRazorpay_order_id(), "FAILED", request.getRazorpay_payment_id(),null);
            return  PaymentDTO.builder()
                    .success(false)
                    .message("Payment signature verification failed")
                    .build();
        }
        //Add credits based on plan
            int creditsToAdd = 0;
        String plan = "BASIC";
        switch (request.getPlanId()){
            case "premium":
                creditsToAdd = 500;
                plan = "PREMIUM";
                break;
            case "ultimate":
                creditsToAdd =5000;
                plan = "ULTIMATE";
                break;
        }
        if(creditsToAdd > 0){
            userCreditService.addCredits(clerkid,creditsToAdd,plan);
            updateTransactionStatus(request.getRazorpay_order_id(), "SUCCESS", request.getRazorpay_payment_id(),creditsToAdd);
            return PaymentDTO.builder()
                    .success(true)
                    .message("Payment verify and credits added successfully")
                    .credits(userCreditService.getCrediteDetails().getCredits())
                    .build();
        } else{
            updateTransactionStatus(request.getRazorpay_order_id(),"FAILED", request.getRazorpay_payment_id(), null);
            return  PaymentDTO.builder()
                    .success(false)
                    .message("Invalid plan selected")
                    .build();
        }
        }catch (Exception e){
            try{
                updateTransactionStatus(request.getRazorpay_order_id(),"ERROR", request.getRazorpay_payment_id(),null);
            } catch (Exception ex){
                throw  new RuntimeException(ex);
            }
            return PaymentDTO.builder()
                    .success(false)
                    .message("Error verifying payment:"+e.getMessage())
                    .build();
        }
    }

    private void updateTransactionStatus(String razorpayOrderId, String status, String razorpayPaymentId, Integer creditsToAdd) {
        paymentTransactionRepository.findAll().stream()
                .filter(t-> t.getOrderId() != null && t.getOrderId().equals(razorpayOrderId))
                .findFirst()
                .map(transsaction->{
                    transsaction.setStatus(status);
                    transsaction.setPaymentId(razorpayPaymentId);
                    if(creditsToAdd != null){
                        transsaction.setCreditsAdded(creditsToAdd);

                    }
                    return  paymentTransactionRepository.save(transsaction);
                })
                .orElse(null);
    }
    private String generateHmachSha256Signature(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException{
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] hmacData = mac.doFinal(data.getBytes());
        return toHexString(hmacData);
    }
    private String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }


}
