package com.example.AgroVert.controller;



import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.AgroVert.repository.WeatherRepository;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.example.AgroVert.model.MyUserDetails;
import com.example.AgroVert.model.Role;
import com.example.AgroVert.model.User;
import com.example.AgroVert.model.Weather;
import com.example.AgroVert.service.UserLoginService;
import com.example.AgroVert.service.WeatherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


@Controller
@RequestMapping("/AgroVert/user")
public class UserController {


	@Autowired
	UserLoginService userLoginService;

	@Autowired
	WeatherService weatherService;

	@Autowired
	WeatherRepository weatherRepository;



	@RequestMapping("/")
	public String viewIndexPage(Model model) {
		User user = new User();
		model.addAttribute("user", user);
		return "index";
	}

	@RequestMapping("/homePage")
	public String viewHomePage(Model model) {
		return "home";
	}


	@PostMapping(path = "/home")
	public String userLogin(@ModelAttribute("user") User user, HttpServletRequest request, Model model) throws JsonMappingException, JsonProcessingException{


		String email = user.getEmail();

		String password = user.getPassword();
		User userDetails = null;
		if(!email.isEmpty() && !password.isEmpty()) {

			userDetails = userLoginService.validateUser(email, password);

			if(userDetails != null) {

				boolean status = userDetails.isEnabled();

				if(status){

					Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, new MyUserDetails(userDetails).getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(auth);

					/*Collection<? extends GrantedAuthority> roles = auth.getAuthorities();
						String roleName = null;
						for(GrantedAuthority role : roles){
							roleName = role.getAuthority();
						}*/


					/*if(roleName.equals("ADMIN")){


							return "admin/project_home";
						}else if(roleName.equals("LEADER")){
							return "leader/home";
						}else{
							return "member/home";
						}*/
					
					
					Weather today = weatherService.getWeatherByMaxId();
					Optional<Weather> temp = weatherService.findById(today.getId()-1);
					Weather yesterday = temp.get();
					Weather week_avg = WeekAvg(today, 0);
					Weather lastweek_avg = WeekAvg(today, 1);
					Weather year_avg = YearAvg(today, 0);
					Weather lastyear_avg = YearAvg(today, 1);

//					Weather today = new Weather();
////					Optional<Weather> temp = weatherService.findById(today.getId()-1);
//					Weather yesterday = new Weather();
//					Weather week_avg = new Weather();
//					Weather lastweek_avg = new Weather();
//					Weather year_avg = new Weather();
//					Weather lastyear_avg = new Weather();

					model.addAttribute("today", today);
					model.addAttribute("yesterday", yesterday);
					model.addAttribute("lastweek_avg", lastweek_avg);
					model.addAttribute("lastyear_avg", lastyear_avg);
					model.addAttribute("week_avg", week_avg);
					model.addAttribute("year_avg", year_avg);
					
					return "home";
				}else{

					return "403";
				}


			}else {

				return "403";

			}
		}else {

			return "403";
		}
	}

	public Weather WeekAvg(Weather today, int last) {
		long id = today.getId();
		if(last ==1) {
			id -= 7;
		}
		Date tod = today.getDate();
		int day = tod.getDay();
		if(day == 0) {
			day = 7;
		}
		Weather week_avg = new Weather();
		double temp =0;
		double humi =0;
		double evapo =0;
		double rain =0;
		
		for(int i =0; i<day; i++) {
			Optional<Weather> temporary = weatherService.findById(id-i);
			Weather temp2 = temporary.get();
			temp += temp2.getTemperature();
			humi += temp2.getHumidity();
			evapo += temp2.getEvapo();
			rain += temp2.getRain();			
		}
		week_avg.setTemperature(temp/day);
		week_avg.setHumidity(humi/day);
		week_avg.setEvapo(evapo/day);
		week_avg.setRain(rain/day);
		
		return week_avg;
	}
	
	public Weather YearAvg(Weather today , int last) {
		long id = today.getId();
		if(last ==1) {
			id -= 364;
		}
		
		int end =0;
		Weather year_avg = new Weather();
		double temp =0;
		double humi =0;
		double evapo =0;
		double rain =0;
		int count = 0;
		
		while(end == 0) {
			id --;
			Optional<Weather> temporary = weatherService.findById(id);
			Weather temp2 = temporary.get();
			Date date = temp2.getDate();
			if(date.getDate() == 1 && date.getMonth() == 1) {
				end = 1;
			}
			temp += temp2.getTemperature();
			humi += temp2.getHumidity();
			evapo += temp2.getEvapo();
			rain += temp2.getRain();
			count ++;
		}
		year_avg.setTemperature(temp/count);
		year_avg.setHumidity(humi/count);
		year_avg.setEvapo(evapo/count);
		year_avg.setRain(rain/count);
		
		return year_avg;
	} 
	
	

	@RequestMapping("/registration")  
	public String newUser(Model model){

		User user = new User();
		model.addAttribute("user", user);

		return "new_user";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveUser(ModelAndView modelAndView, @ModelAttribute("user") User user) {


		ObjectMapper mapper = new ObjectMapper();
		ObjectNode userNode = mapper.createObjectNode();
		String email = user.getEmail();

		String password = user.getPassword();
		if(email != null && email.length() > 0 && password != null && password.length() > 0){

			Optional<User> isExist = userLoginService.isUserExist(email);

			if(isExist.isPresent()) {
				userNode.put("Status", "0");
				userNode.put("Message","User already exists");

				return "403";
			}else{

				Role role = new Role();
				role.setId(1);
				role.setName("USER");

				Set<Role> roles = new HashSet<>();
				roles.add(role);
				user.setRoles(roles);

				user.setEnabled(true);	
				userLoginService.save(user);



			}
		}else {

			userNode.put("Status", "0");
			userNode.put("Message","Request body parameter missing");

			return "403";
		}


		return "redirect:/AgroVert/user/";
	}



	@RequestMapping("/logout")  
	public String logout(Model model, HttpServletRequest request, HttpServletResponse response){

		SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);

		User user = new User();
		model.addAttribute("user", user);
		return "logout";
	}
	
	@RequestMapping("/index")  
	public String index(Model model){


		return "index";
	}

	@RequestMapping("/about")  
	public String about(Model model){


		return "about";
	}
	
	@RequestMapping("/gallery")  
	public String gallery(Model model){


		return "gallery";
	}

	@RequestMapping("/charts")
	public String charts(Model model){
		model.addAttribute("chartData", getChartData());
		return "charts";
	}


	private List<List<Object>> getChartData() {
		return Arrays.asList(
				Arrays.asList("2021", weatherRepository.getAvg2021(), 12),
				Arrays.asList("2020", weatherRepository.getAvg2020(), 14),
				Arrays.asList("2019", weatherRepository.getAvg2029(), 13)
		);
	}

	@RequestMapping("/connexion")  
	public String connexion(Model model){

		User user = new User();
		model.addAttribute("user", user);
		return "connexion";
	}


	@RequestMapping(value = "/saveWeather", method = RequestMethod.GET)
	public String saveWeather(ModelAndView modelAndViewr) {


		ObjectMapper mapper = new ObjectMapper();
		ObjectNode userNode = mapper.createObjectNode();


		List<Weather> weatherList = new ArrayList<Weather>();
		
		try {
			String excelPath = "C:\\Users\\amine\\OneDrive\\Bureau\\Meteo19-21.xlsx";
			XSSFWorkbook workbook;
			workbook = new XSSFWorkbook(excelPath);
			XSSFSheet sheet = workbook.getSheet("feuil1");
			int size = getRowCount(sheet);
			for(int i=2; i<size; i++) {
				Weather weather1 = new Weather();
				weather1.setTemperature(getCell(sheet, i, 1));
				weather1.setMin_temperature(getCell(sheet, i, 2));
				weather1.setMax_temperature(getCell(sheet, i, 3));
				weather1.setHumidity(getCell(sheet, i, 4));
				weather1.setMin_humidity(getCell(sheet, i, 5));
				weather1.setMax_humidity(getCell(sheet, i, 6));
				weather1.setTemp_rosee(getCell(sheet, i, 7));
				weather1.setWind_speed(getCell(sheet, i, 8));
				weather1.setWind_direction(getCell(sheet, i, 9));
				weather1.setRayo(getCell(sheet, i, 10));
				weather1.setRain(getCell(sheet, i, 11));
				weather1.setEvapo(getCell(sheet, i, 12));
				weather1.setDate(sheet.getRow(i).getCell(0).getDateCellValue());
				
				weatherList.add(weather1);
			}
			for(Weather weather : weatherList) {
				weatherService.save(weather);
			}
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:/AgroVert/user/";
	}

	public static int getRowCount(XSSFSheet sheet) {
		int rowCount = sheet.getPhysicalNumberOfRows();
		System.out.println("Number of rows:" + rowCount);
		return rowCount;
	}
	
	public static double getCell(XSSFSheet sheet, int row, int col) {
		try {
			double value = sheet.getRow(row).getCell(col).getNumericCellValue();
			return value;
		}catch (Exception e) {
			return 0;
		}
	}


}
