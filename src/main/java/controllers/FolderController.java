package controllers;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import domain.Actor;
import domain.Folder;

import services.ActorService;
import services.FolderService;

@Controller
@RequestMapping("folder")
public class FolderController extends AbstractController {

	// Services
	
	@Autowired
	private FolderService folderService;
	@Autowired
	private ActorService actorService;
	
	// List
	
	@RequestMapping("list")
	public ModelAndView list(@RequestParam(required = false) Integer parentId) {
		ModelAndView res = new ModelAndView("folder/list");
		Actor actor = this.actorService.findPrincipal();
		Collection<Folder> folders = null;
		if(parentId == null) {
			folders = this.folderService.findByActorWithoutParent(actor);
		} else {
			Folder parent = this.folderService.findOne(parentId);
			this.folderService.findByActorAndParent(actor, parent);
		}
		res.addObject("folders", folders);
		res.addObject("requestURI", "folder/list.do");
		return res;
	}
	
	// Create
	
	@SuppressWarnings("unused")
	@RequestMapping("create")
	private ModelAndView create() {
		Actor actor = this.actorService.findPrincipal();
		Folder folder = this.folderService.create(actor);
		ModelAndView res = this.createEditModelAndView(folder);
		return res;
	}
	
	// Edit
	
	@SuppressWarnings("unused")
	@RequestMapping("edit")
	private ModelAndView edit(@RequestParam(required = true) Integer folderId) {
		Folder folder = this.folderService.findOne(folderId);
		Assert.notNull(folder);
		ModelAndView res = this.createEditModelAndView(folder);
		return res;
	}
	
	@SuppressWarnings("unused")
	@RequestMapping(value = "edit", method = RequestMethod.POST, params = "save")
	private ModelAndView save(@Valid Folder folder, BindingResult binding) {
		ModelAndView res = null;
		if(binding.hasErrors()) {
			this.createEditModelAndView(folder);
		} else {
			try {
				this.folderService.save(folder);
				res = new ModelAndView("redirect:list.do");
			} catch(Throwable t) {
				res = new ModelAndView("cannot.commit.error");
			}
		}
		return res;
	}
	
	// Delete
	
	@SuppressWarnings("unused")
	@RequestMapping(value = "edit", method = RequestMethod.POST, params = "delete")
	private ModelAndView delete(@Valid Folder folder, BindingResult binding) {
		ModelAndView res = null;
		if(binding.hasErrors()) {
			this.createEditModelAndView(folder);
		} else {
			try {
				this.folderService.delete(folder);
				res = new ModelAndView("redirect:list.do");
			} catch(Throwable t) {
				res = new ModelAndView("cannot.commit.error");
			}
		}
		return res;
	}
	
	// Ancillary methods
	
	private ModelAndView createEditModelAndView(Folder folder) {
		return createEditModelAndView(folder, null);
	}
	
	private ModelAndView createEditModelAndView(Folder folder, String message) {
		ModelAndView res = new ModelAndView("folder/edit");
		res.addObject("folder", folder);
		res.addObject("message", message);
		return res;
	}
	
}