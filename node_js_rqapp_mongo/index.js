require('dotenv/config');


var mongodb = require('mongodb');
var ObjectID = mongodb.ObjectID;
var express = require('express');
const bodyParser = require('body-parser');
const cron = require('node-cron');
const fs = require('fs');


//Create express service
var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));


//Create MongoDB Client
var MongoClient = mongodb.MongoClient;

//Connection url
var url = process.env.DB_CONNECTION; // 27017 default port 

//Token to mapbox
var MAPBOX_TOKEN = process.env.MAPBOX_TOKEN;



//Middlewares (runs in background ones a user post something to the database)
/*app.use('/vibrations',()=>{
	console.log('Middleware running...');

})*/

/* ┌────────────── second (optional)
 # │ ┌──────────── minute
 # │ │ ┌────────── hour
 # │ │ │ ┌──────── day of month
 # │ │ │ │ ┌────── month
 # │ │ │ │ │ ┌──── day of week
 # │ │ │ │ │ │
 # │ │ │ │ │ │
 # * * * * * **/
 //Update JSON file every five hours
 cron.schedule(' * */5 * * *', () => {
    console.log('Updating  JSON file');
	updateJSONFile();
	
 }, {
   timezone: "Europe/Bucharest"
 });

//MongoDB 
MongoClient.connect(url,{useNewUrlParser:true, useUnifiedTopology: true },function(err,client){
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


//Update JSON file
function updateJSONFile(){
	MongoClient.connect(url,{useNewUrlParser:true, useUnifiedTopology: true },function(err,client){
		if(err){
			console.log('Unable to connect to mongoDB server',err);
		}else{
			var db = client.db('rqdb');
			let cursor = db.collection('vibrations').find({}).toArray(function(err, result){	
				if(err){
					console.log('Eroare la accesarea datelor');
				}else{
					  
					 var point;
					 let coords;
					 const mapBoxObject = {
							type: 'FeatureCollection',
							features:[							
	
									]
							};
							
					
					result.forEach(function (item){
						coords =[parseFloat(item._id.lng) , parseFloat(item._id.lat)];  // <- aici pun coordonatele (care in db sunt double) și mi le converteste la string ["232.3","-12.233"]
					
						
						point = {
							type: 'Feature',
							properties: {},
							geometry: {
								type: 'Point',
								coordinates: coords    // <- aici le pun în obiectul de tip punct pe harta și vreau să arate așa: [232.3,-12.233]
							}
						};
					
					mapBoxObject.features.push(point); // <- aici pun punct cu punct in obiectul final
					
					});
					
					var resultObj = JSON.stringify(mapBoxObject);  
					 // write JSON string to a file
					fs.writeFile('map.json', resultObj, (err) => { // <- aici scriu in fisier json-ul
						if (err) {
							throw err;
						}
						console.log("JSON data is saved.");
						
						updateMapBoxTile();  // <- aici trimit la mapbox punctele
					});
				}
			});
		}
	});
}

//Send to mapBox new tileset stored 
function updateMapBoxTile(){
	var upload = require('mapbox-upload');

	// creates a progress-stream object to track status of
	// upload while upload continues in background
	var progress = upload({
		file: __dirname + '/map.json', // Path to mbtiles file on disk.
		account: 'bogda23', // Mapbox user account.
		accesstoken: MAPBOX_TOKEN, // A valid Mapbox API secret token with the uploads:write scope enabled.
		mapid: 'bogda23.ckclve4vk1cvy2dn35iizbu1j-6wrn6', // The identifier of the map to create or update.
		name: 'My upload' // Optional name to set, otherwise a default such as original.geojson will be used.
	});

	progress.on('error', function(err){
		if (err){
			throw err;
		}
	});

	progress.on('progress', function(p){
		console.log("Uploading tileset.... "+p.percentage +" %");
	});

	progress.once('finished', function(){
		console.log("Done uploading tileset")
	});
}