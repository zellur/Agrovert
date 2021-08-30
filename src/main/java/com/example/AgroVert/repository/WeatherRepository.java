package com.example.AgroVert.repository;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.AgroVert.model.Weather;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long>{

	@Query(value="select * from Weather where id=(select max(id) from Weather)",nativeQuery=true)
	 Weather getWeatherByMaxId();
	
	
	
	Weather findWeatherByDate(Date date);
}
