package springdata.querydsl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {
	@Autowired
	EntityManager em;

}
