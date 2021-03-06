package com.seckill.web;

import com.seckill.dto.Exposer;
import com.seckill.dto.SeckillExecution;
import com.seckill.dto.SeckillResult;
import com.seckill.entity.Seckill;
import com.seckill.enums.SeckillStatEnum;
import com.seckill.exception.RepeatKillException;
import com.seckill.exception.SeckillCloseException;
import com.seckill.service.SeckillService;

import java.util.Date;
import java.util.List;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller//@Service @Component
@RequestMapping("/seckill")
public class SeckillController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SeckillService seckillService;
	
	@RequestMapping(name="/list",method = RequestMethod.GET)
	public String list(Model model) {

		List<Seckill> list = seckillService.getSeckillList();
		model.addAttribute("list",list);
		
		return "list";
		
	}
	
	
	@RequestMapping(value="/{seckillId}/detail",method = RequestMethod.GET)
	public String detail(@PathVariable("seckillId") Long seckillId,Model model) {
		if(seckillId == null) {
			return "redirect:/seckill/list";
		}
		Seckill seckill = seckillService.getById(seckillId);

		if(seckill == null) {
			return "forward:/seckill/list";
		}
		
		model.addAttribute("seckill",seckill);
		
		return "detail";
	}

	//public void/*TODO*/ exposer(Long seckillId) {
	@RequestMapping(value="/{seckillId}/exposer",
			method = RequestMethod.POST
			//produces = {"application/json;charset=UTF-8"}
			)
	@ResponseBody
	public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {
		SeckillResult<Exposer> result;
		
		try {

			Exposer exposer = seckillService.exportSeckillUrl(seckillId); 
			result = new SeckillResult<Exposer>(true,exposer);
			
		} catch(Exception e){
			logger.error(e.getMessage(),e);
			result = new SeckillResult<Exposer>(false,e.getMessage());
			
		}

		

		return result;
				
	}
		
	@RequestMapping(value = "/{seckillId}/{md5}/execution",
			method = RequestMethod.POST
		//	produces = {"application/json;charset= UTF-8"}
			)
	@ResponseBody
	public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
			@PathVariable("md5") String md5,
			@CookieValue(value = "userPhone",required = false) Long phone){

		if(phone == null ) {
			return new SeckillResult<SeckillExecution>(false,"鎵嬫満鍙蜂负绌�");
			
		}

		SeckillResult<SeckillExecution> result;

		try {
			
			SeckillExecution execution1 = seckillService.executeSeckillProcedure(seckillId,phone,md5);
			return new SeckillResult<SeckillExecution>(true,execution1);

		}catch(RepeatKillException e1){
			SeckillExecution execution = new SeckillExecution(seckillId,SeckillStatEnum.REPEAT_KILL);

			return new SeckillResult<SeckillExecution>(true,execution);
			
		}catch(SeckillCloseException e2) {
			SeckillExecution execution = new SeckillExecution(seckillId,SeckillStatEnum.END);
			return new SeckillResult<SeckillExecution>(true,execution);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
			return new SeckillResult<SeckillExecution>(true, execution);
		}
	}
	
	@RequestMapping(value = "/time/now", method = RequestMethod.GET)
	@ResponseBody
	public SeckillResult<Long> time() {

		Date now = new Date();

		return new SeckillResult(true,now.getTime());
		
	}
}
