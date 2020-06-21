require('dotenv/config');


var mongodb = require('mongodb');
var ObjectID = mongodb.ObjectID;
var express = require('express');
const bodyParser = require('body-parser');


//Create express service
var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
//app.use(bodyParser.urlencoded({extended: true}));

//Create MongoDB Client
var MongoClient = mongodb.MongoClient;

//Connection url
var url = process.env.DB_CONNECTION; // 27017 default port 


//Middlewares
/*
app.use('/vibrations',()=>{
	console.log('Middleware running...');
})
*/

//MongoDB 

MongoClient.connect(url,{useNewUrlParser:true},{ useUnifiedTopology: true },function(err,client){
	if(err){
		console.log('Unable to connect to mongoDB server',err);
	}else{
		
		//Post
		app.post('/vibrations',(request,response,next)=>{
			
			console.log(request.body);
			var post_data = request.body;
			
			var lat =post_data.lat;
			var lng = post_data.lng;
			var vibration_value = post_data.vibration_value;
			var last_update = post_data.last_update;
			var country_name = post_data.country_name;
			var iso_code = post_data.iso_code;
			var county_name = post_data.county_name;
			
			
			var insertJson = {
				'_id':{ 'lat':lat,
						'lng':lng
					  },
				'vibration_value':vibration_value,
				'last_update': new Date(),
				'country_name':country_name,
				'iso_code':iso_code,
				'county_name':county_name
			};
			
			var updateJson = {
				'vibration_value':post_data.vibration_value,
				'last_update':new Date()
			};
			
			var db = client.db('rqdb');
			
			//Check if exists lat lng
			db.collection('vibrations').find({'_id':{ 'lat':lat,'lng':lng }}).count(function(err,number){
						  
				if(number != 0 ){
	
					try{
						db.collection('vibrations').updateOne({'_id':{ 'lat':lat,'lng':lng }},
						{$set:updateJson},function(err,res){
						if(err){
							
							response.json('Eroare la update');
							console.log('Eroare la update');
						}else{
							response.json('Updating existing lat/lng');
							console.log('Updating existing lat/lng');
						
						}
					});
					
					}catch(e){
						console.log(e);
					}
					
					
				}else{
					//Insert data
					db.collection('vibrations').insertOne(insertJson,function(err,res){
						if(err){
							response.json('Eroare la inserare');
							console.log('Eroare la inserare' + err);
						}else{
							response.json('Inserting new data lat/lng');
							console.log('Inserting new data lat/lng');
							
						}
					
					});
				}
			
			
			});
		});
		
		//Get 
		app.get('/info',(request,response,next)=>{
		
			var db = client.db('rqdb');
			var get_data = request.query;
			
			var county_name = get_data.county_name;
			var country_name = get_data.country_name;
			var iso_code = get_data.iso_code;
			
			if(county_name != null){
				let query = {'county_name':county_name};
				let cursor = db.collection('vibrations').find(query).toArray(function(err, result){	
					if(err){
						console.log('Eroare la accesarea datelor');
					}else{
						 var resultObj = {'list':result}
						 console.log(resultObj); 
						 response.end(JSON.stringify(resultObj));
					}
				});
				
			}else if(country_name != null){
				let query = {'country_name':country_name};
				let cursor = db.collection('vibrations').find(query).toArray(function(err, result){
					if(err){
						console.log('Eroare la accesarea datelor');
					}else{
					 var resultObj = {'list':result}
						 console.log(resultObj); 
						 response.end(JSON.stringify(resultObj));
					}
				});
			}else if(iso_code != null){
				let query = {'iso_code':iso_code};
				
				let cursor = db.collection('vibrations').find(query).toArray(function(err, result){
					if(err){
						console.log('Eroare la accesarea datelor');
					}else{
						 var resultObj = {'list':result}
						 console.log(resultObj); 
						 response.end(JSON.stringify(resultObj));
					}
				});
			}else{
				
				var resultObj = {'error':'No field selected'};
				 response.end(JSON.stringify(resultObj));
			}
				
			
		});
		
		//Start web server
		app.listen(3000,()=>{
			console.log('Connected to MongoDB server, running on port 3000');
		});
	}
	
});