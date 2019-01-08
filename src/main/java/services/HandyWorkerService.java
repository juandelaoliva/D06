
package services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import repositories.HandyWorkerRepository;
import security.UserAccount;
import domain.Application;
import domain.Complaint;
import domain.Customer;
import domain.FixupTask;
import domain.HandyWorker;
import domain.Note;
import domain.Phase;
import domain.Report;
import domain.Url;
import domain.WorkPlan;

@Service
@Transactional
public class HandyWorkerService {

	//Managed Repository

	@Autowired
	private HandyWorkerRepository	handyWorkerRepository;

	// Supporting Service

	@Autowired
	private FixupTaskService		fixupTaskService;

	@Autowired
	private ServiceUtils			serviceUtils;

	@Autowired
	private ActorService			actorService;

	@Autowired
	private ApplicationService		applicationService;

	@Autowired
	private ReportService			reportService;

	@Autowired
	private WorkPlanService			workPlanService;


	//constructor

	public HandyWorkerService() {
		super();
	}

	// Simple CRUD methods

	public HandyWorker create() {
		final HandyWorker hw = new HandyWorker();

		hw.setUserAccount(new UserAccount());

		return hw;
	}

	public Collection<HandyWorker> findAll() {
		return this.handyWorkerRepository.findAll();
	}

	public Collection<HandyWorker> findAll(final int handyWorkerId) {
		Collection<HandyWorker> hw;

		hw = this.handyWorkerRepository.findAll();
		Assert.notNull(hw);

		return hw;
	}

	public HandyWorker findOne(final int handyWorkerId) {
		Assert.notNull(this);
		return this.handyWorkerRepository.findOne(handyWorkerId);
	}

	public HandyWorker save(final HandyWorker hw) {
		//	final HandyWorker nuevo = this.checkObjectSave(hw);
		//		if (hw.getId() == 0) {
		//			hw.setApplications(new ArrayList<Application>());
		//			hw.setWorkPlans(new ArrayList<WorkPlan>());
		//			hw.setTutorials(new ArrayList<Tutorial>());
		//			hw.setReceivedEndorsements(new ArrayList<Endorsement>());
		//			hw.setSendedEndorsements(new ArrayList<Endorsement>());
		//			hw.setSocialProfiles(new ArrayList<SocialProfile>());
		//			hw.setFolders(new ArrayList<Folder>());
		//			hw.setReceivedMessages(new ArrayList<Message>());
		//			hw.setSendedMessages(new ArrayList<Message>());
		//			hw.setUserAccount(new UserAccount());

		//			
		//		} else {
		//			hw.setApplications(nuevo.getApplications());
		//			hw.setWorkPlans(nuevo.getWorkPlans());
		//			hw.setTutorials(nuevo.getTutorials());
		//			hw.setReceivedEndorsements(nuevo.getReceivedEndorsements());
		//			hw.setSendedEndorsements(nuevo.getSendedEndorsements());
		//			hw.setSocialProfiles(nuevo.getSocialProfiles());
		//			hw.setFolders(nuevo.getFolders());
		//			hw.setReceivedMessages(nuevo.getReceivedMessages());
		//			hw.setSendedMessages(nuevo.getSendedMessages());
		//			hw.setUserAccount(new UserAccount());

		//		}
		Assert.notNull(hw);
		;
		return this.handyWorkerRepository.save(hw);
	}

	public void delete(final HandyWorker hw) {
		Assert.notNull(hw);

		Assert.isTrue(hw.getId() != 0);
		this.handyWorkerRepository.delete(hw);
	}

	//Other methods

	public Map<String, Collection<HandyWorker>> fixupTasksTop3() {
		final Collection<HandyWorker> collection = this.handyWorkerRepository.getTop3HandyWorkerWithMoreComplaints();
		final Map<String, Collection<HandyWorker>> res = new HashMap<>();

		res.put("Collection", collection);

		return res;

	}

	public Collection<HandyWorker> getTop3HandyWorkerWithMoreComplaints() {
		final Collection<HandyWorker> ratio = this.handyWorkerRepository.getTop3HandyWorkerWithMoreComplaints();
		return ratio;
	}

	public Collection<HandyWorker> listHandyWorkerApplication() {
		final Collection<HandyWorker> list = this.handyWorkerRepository.listHandyWorkerApplication();
		return list;
	}

	//un handy worker lista todos los fixupTasks
	public Collection<FixupTask> getAllFixupTasks() {
		Collection<FixupTask> res;
		res = this.fixupTaskService.findAll();
		return res;
	}

	public Collection<FixupTask> getFixupTaskFromCustomer(final Customer customer) {
		Collection<FixupTask> res;
		res = this.fixupTaskService.findByCustomer(customer);
		return res;
	}

	public void banActor(final HandyWorker hw) {
		Assert.notNull(hw);
		this.serviceUtils.checkAuthority("ADMIN");
		hw.getUserAccount().setBanned(true);
		this.handyWorkerRepository.save(hw);
	}

	public void unbanActor(final HandyWorker hw) {
		Assert.notNull(hw);
		this.serviceUtils.checkAuthority("ADMIN");
		hw.getUserAccount().setBanned(false);
		this.handyWorkerRepository.save(hw);
	}

	public HandyWorker findByAssignedFixupTask(final FixupTask f) {
		final FixupTask fixupTask = (FixupTask) this.serviceUtils.checkObject(f);
		return this.handyWorkerRepository.findByAssignedFixupTask(fixupTask.getId());
	}

	public boolean isSuspicious(final HandyWorker h) {
		final HandyWorker handyWorker = (HandyWorker) this.serviceUtils.checkObject(h);
		Boolean res = false;
		for (final Application a : this.applicationService.findApplicationsByHandyWorker(handyWorker))
			if (this.actorService.containsSpam(a.getWorkerComments())) {
				res = true;
				break;
			}
		for (final FixupTask f : this.fixupTaskService.findAcceptedFixupTasksByHandyWorker(handyWorker))
			for (final Complaint com : f.getComplaints()) {
				for (final Url u : com.getAttachments())
					if (this.actorService.containsSpam(u.getUrl())) {
						res = true;
						break;
					}
				if (this.actorService.containsSpam(com.getDescription())) {
					res = true;
					break;
				}
				final Report report = this.reportService.findByComplaint(com);
				for (final Note n : report.getNotes())
					for (final String comment : n.getComments())
						if (this.actorService.containsSpam(comment)) {
							res = true;
							break;
						}
			}
		for (final WorkPlan w : this.workPlanService.findWorkPlanByHandyWorker(handyWorker))
			for (final Phase p : w.getPhases())
				if (this.actorService.containsSpam(p.getDescription()) || this.actorService.containsSpam(p.getTitle())) {
					res = true;
					break;
				}
		return res;
	}

}
