package springdata.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import entity.QHello;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import springdata.querydsl.entity.Hello;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {
	@Autowired
	EntityManager em;
	@Test
	void contextLoads(){
		Hello hello=new Hello();
		em.persist(hello);

		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		QHello qHello = new QHello("h");

		Hello result = queryFactory
				.selectFrom(qHello)
				.fetchOne();

		assertThat(result).isEqualTo(hello);
	}



}
