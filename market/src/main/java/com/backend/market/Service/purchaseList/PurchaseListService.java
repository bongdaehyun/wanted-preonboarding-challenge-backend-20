package com.backend.market.Service.purchaseList;

import com.backend.market.Common.auth.UserDetailsImpl;
import com.backend.market.DAO.Entity.*;
import com.backend.market.Repository.MemberRepository;
import com.backend.market.Repository.ProductRepository;
import com.backend.market.Repository.PurchaseListRepository;
import com.backend.market.Request.ProductReq;
import com.backend.market.Service.Product.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PurchaseListService {

    private final PurchaseListRepository purchaseListRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    //구매하기 버튼 눌렀을 시
    public void addPurchasList(ProductReq productReq, UserDetailsImpl userDetails)
    {
        Member member = isExistUser(userDetails);
        if(member == null) return;

        if(!productReq.getStatus().equals(Status.sale))
        {
            return ;
        }

        //제품 상태 변경 : 판매 -> 예약

        updateProductStatus(productReq);

        PurchaseList purchaseList = new PurchaseList();
        //등록한 판매자의 아이디
        Long id = productReq.getMember().getUserId();

        purchaseList.setProductId(productReq.getProduct_id());
        purchaseList.setBuyerId(member.getUserId());
        purchaseList.setOrderStatus(OrderStatus.reservation);
        purchaseList.setSellerId(id);
        purchaseList.setCreaeDate(LocalDate.now());

        this.purchaseListRepository.save(purchaseList);
    }

    //제품관련 정보와 현재 조회한 회원의 고유번호
    public List<PurchaseList> getTransHistory(ProductReq productReq,Long id,UserDetailsImpl userDetails)
    {
        Member member = isExistUser(userDetails);
        if(member == null) return new ArrayList<>();
        //판매자인 경우
        if(productReq.getMember().getUserId().equals(id))
        {
            return this.purchaseListRepository.findAllBySellerId(id);
        }else{//구매자인 경우
            return this.purchaseListRepository.findAllByBuyerId(id);
        }
    }

    private void updateProductStatus(ProductReq productReq)
    {
        productReq.setStatus(Status.reservation);

        Optional<Product> product = productRepository.findById(productReq.getProduct_id());
        if(product.isEmpty()) return;
        Product p = product.get();
        p.setStatus(productReq.getStatus());
        this.productRepository.save(p);
    }


    private Member isExistUser(UserDetailsImpl userDetails){
        Long userId = userDetails.getUser().getUserId();
        Optional<Member> findMember = memberRepository.findById(userId);
        return findMember.orElse(null);
    }
}
