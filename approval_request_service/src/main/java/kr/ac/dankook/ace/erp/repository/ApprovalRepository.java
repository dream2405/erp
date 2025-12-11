package kr.ac.dankook.ace.erp.repository;

import kr.ac.dankook.ace.erp.document.Approval;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApprovalRepository extends MongoRepository<Approval, String> {
    java.util.List<Approval> findByRequesterId(Integer requesterId);
}
