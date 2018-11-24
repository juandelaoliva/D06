
package services;

import java.util.Collection;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import repositories.EducationRecordRepository;
import domain.EducationRecord;

@Service
@Transactional
public class EducationRecordService {

	//---------------Managed Repository---------------------------------------

	@Autowired
	private EducationRecordRepository	educationRecordRepository;


	//---------------Supporting Services----------------------------------------

	//------------------Simple CRUD methods----------------------------------
	public EducationRecord create() {
		EducationRecord res;
		res = new EducationRecord();
		return res;
	}

	public Collection<EducationRecord> findAll() {
		Collection<EducationRecord> res;
		res = this.educationRecordRepository.findAll();
		return res;
	}

	public EducationRecord save(final EducationRecord educationRecord) {
		Assert.notNull(educationRecord);
		EducationRecord res;
		res = this.educationRecordRepository.save(educationRecord);
		return res;
	}

	public void delete(final EducationRecord educationRecord) {
		Assert.notNull(educationRecord);
		this.educationRecordRepository.delete(educationRecord);
	}

}
