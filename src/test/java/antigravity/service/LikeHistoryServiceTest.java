package antigravity.service;

import antigravity.DataBaseCleanUp;
import antigravity.common.BaseException;
import antigravity.entity.Member;
import antigravity.entity.Product;
import antigravity.repository.LikeHistoryRepository;
import antigravity.repository.MemberRepository;
import antigravity.repository.ProductRepository;
import antigravity.repository.ProductViewCountRepository;
import java.math.BigDecimal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LikeHistoryServiceTest {
    @Autowired
    LikeHistoryRepository likeHistoryRepository;
    @Autowired
    DataBaseCleanUp dataBaseCleanUp;
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    LikeHistoryService likeHistoryService;

    @Autowired
    ProductViewCountRepository productViewCountRepository;
    Member alreadySaveMember;

    Product alreadySaveProduct;

    @BeforeEach
    public void setup() {
        dataBaseCleanUp.cleanUp();
        Member member = Member.builder().email("test").name("test").build();
        alreadySaveMember = memberRepository.save(member);
        Product product = Product.builder().sku("test").price(BigDecimal.ONE).name("testprodcut").quantity(10).build();
        alreadySaveProduct = productRepository.save(product);
    }

    @Test
    public void alreadySaveMemberDifferentProductTest() {
        Product product = Product.builder().sku("test").price(BigDecimal.TEN).name("testprodcut").quantity(10).build();
        product = productRepository.save(product);
        likeHistoryService.addNotDuplicatedLikeHistory(product.getId(), alreadySaveMember.getId());
        likeHistoryService.addNotDuplicatedLikeHistory(alreadySaveProduct.getId(), alreadySaveMember.getId());
        int expect = 2;
        Assertions.assertThat(expect).isEqualTo(likeHistoryRepository.findAll().size());
    }

    @Test
    public void alreadySaveProductDifferentMemberTest() {
        Member member = Member.builder().email("test").name("test1").build();
        member = memberRepository.save(member);
        likeHistoryService.addNotDuplicatedLikeHistory(alreadySaveProduct.getId(), alreadySaveMember.getId());
        likeHistoryService.addNotDuplicatedLikeHistory(alreadySaveProduct.getId(), member.getId());
        int expect = 2;
        Assertions.assertThat(expect).isEqualTo(likeHistoryRepository.findAll().size());
    }

    @Test
    public void errorAddTest() {
        likeHistoryService.addNotDuplicatedLikeHistory(alreadySaveProduct.getId(), alreadySaveMember.getId());

        Assertions.assertThatThrownBy(() -> likeHistoryService.addNotDuplicatedLikeHistory(alreadySaveProduct.getId(),
                        alreadySaveMember.getId()))
                .isInstanceOf(BaseException.class);
    }

    @Test
    public void addCountTest() {
        likeHistoryService.addNotDuplicatedLikeHistory(alreadySaveProduct.getId(), alreadySaveMember.getId());
        Long expect = 1L;
        Assertions.assertThat(expect).isEqualTo(productViewCountRepository.findAll().get(0).getCount());
    }

}