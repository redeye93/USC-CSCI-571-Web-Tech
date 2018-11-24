<?php
	define("APP_KEY", "<Google place api key>");
	$keyword = "";
	$category = "default";
	$distance = "";
	$inputLocation = "";
	$location = "here";
	$here = "";
	$table = null;
	$lat = 999999;
	$lng = 999999;
	$images_length = -1;
	$reviews = [];
	$photos = [];
	$name = "";
	$page_state = 0;

	// If there is any request for the search
	if(isset($_POST['keyword']) && (isset($_POST['inputLocation']) || isset($_POST['here']))){
		$keyword = $_POST['keyword'];
		$category = $_POST['category'];
		if(strlen($_POST['distance'])>0){
			$distance = (int)$_POST['distance'];
		}
		$location = $_POST['location'];
		$here = json_decode($_POST["here"], true);
		$lat = $here["lat"];
		$lng = $here["lon"];

		// Making it global to set the ssl false for file_get_contents
		$arrContextOptions=array(
		    "ssl"=>array(
		        "verify_peer"=>false,
		        "verify_peer_name"=>false,
		    ),
		);

		//If the user has input any custom search place as his location
		if(isset($_POST['inputLocation'])){
			$inputLocation = $_POST['inputLocation'];
		}

		// Check whether the request is trying to search for nearby places or a particular place
		if(isset($_POST["placeid"])) {
			$page_state = 2;
			//Call the google places api
			$result = json_decode(file_get_contents("https://maps.googleapis.com/maps/api/place/details/json?placeid=".$_POST["placeid"]."&key=".APP_KEY, false, stream_context_create($arrContextOptions)), true);

			if($result["status"]=="OK" && isset($result["result"])){
				//Now set the application to call for the photos.
				if(isset($result["result"]["reviews"])){
					$reviews = $result["result"]["reviews"];
				}

				//Store the photo references
				if(isset($result["result"]["photos"])){
					$photos = $result["result"]["photos"];
				}

				// Check for first 5 images or the image list length 
				$images_length = count($photos)>5?5:count($photos);

				for($index=0; $index<$images_length; $index++){
					$img = "./".$index.".jpg";
					file_put_contents($img, file_get_contents("https://maps.googleapis.com/maps/api/place/photo?maxwidth=1000&photoreference=".$photos[$index]["photo_reference"]."&key=".APP_KEY, false, stream_context_create($arrContextOptions)));
				}

				$name = $result["result"]["name"];
			} else{
				// Response issue from the api
			}

		} else {
			//Check for the specific location
			$page_state = 1;
			if($location!="here") {
				$result = json_decode(file_get_contents("https://maps.googleapis.com/maps/api/geocode/json?address=".urlencode($inputLocation)."&key=".APP_KEY, false, stream_context_create($arrContextOptions)), true);

				if($result["status"]=="OK"){
					$lat = $result["results"][0]["geometry"]["location"]["lat"];
					$lng = $result["results"][0]["geometry"]["location"]["lng"];
				} else {
					$lat = null;
				}
			}

			// Location api fails
			if($lat != null){
				$temp_distance = $distance!=""?$distance*1609.34:16093.4;
				//Call the nearby places api
				$url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=".$lat.",".$lng."&radius=".$temp_distance."&keyword=".urlencode($keyword)."&key=".APP_KEY;
				if($category!="default"){
					$url = $url."&type=".$category;
				}
				
				$result = json_decode(file_get_contents($url, false, stream_context_create($arrContextOptions)), true);

				//Check if there is a results property in the response
				if(isset($result["results"])){
					$table = [];
					foreach($result["results"] as $row){
						$temp = [];
						$temp["icon"] = $row["icon"];
						$temp["name"] = $row["name"];
						$temp["vicinity"] = $row["vicinity"];
						$temp["placeid"] = $row["place_id"];
						$temp["lat"] = $row["geometry"]["location"]["lat"];
						$temp["lng"] = $row["geometry"]["location"]["lng"];
						array_push($table,$temp);
					}
				} else {
					//Error so print no results found
					$table = [];
				}
			} else{
				// Unknown location
				$table = [];
			}
		}
	}
?>
<html>
	<head>
		<title>Travel and Entertainment Search</title>
		<style type="text/css">
			label{
				font-weight: bold;
			}
			table{
				width: 1100px;
				margin: auto;
				border: 2px solid #DDDDDD;
				border-collapse: collapse;
			}
			td, th{
				border: 2px solid #DDDDDD;	
			}
			a{
				cursor: pointer;
			}
			input[type=text]{
				max-width: 160px;
			}
			.row{
				width: 100%;
				margin: 0px;
			}
			.mcenter{
				margin: auto;
			}
			.tcenter{
				text-align: center;
			}
			.box{
				border: 2px solid #dfdfdf;
				background-color: #fafafa;
				width: 600px;
				height: 220px;
				padding: 10px;
				margin-top: 10px;
			}
			.heading{
				border-bottom: 2px solid #c5c5c5;
				padding-bottom: 5px;
				font-style: italic;
				font-weight: 500;
				font-size: 25px;
				text-align: center;
				margin-bottom: 10px;
			}
			.noresults{
				background-color: #efefef;
				border: 2px solid #dcdcdc;
				width: 800px;
				margin:auto;
				text-align: center;
			}
			.topmargin{
				margin-top: 15px;
			}
			.arrow{
				border: solid black;
				border-width: 0 2px 2px 0;
				display: inline-block;
				padding: 6px;
			}
			.map_box{
				position: absolute !important;
				height: 300px;
				width: 400px;
				z-index: 3;
			}
			.direction_options{
				position: absolute;
				display: none;
			}
			.direction_option_box{
				width: 100px;
				background-color: #f0eef0;
				display: block;
				z-index: 4;
			}
			.direc_item{
				padding: 10px;
				cursor: pointer;
			}
			.direc_item:hover{
				background-color: #d8d7d8;
			}
			#reviewButton, #photoButton{
				cursor: pointer;
			}
		</style>
	</head>
	<body style="margin: 0px; padding:0px;" id="body">
		<form method="POST" action="place.php" id="myForm">
			<div class="row">
				<div class="mcenter box">
					<div class="heading">Travel and Entertainment Search</div>
					<div class="row">
						<div class="row" style="margin-bottom: 5px;">
							<label for="keyword">Keyword</label>
							<input type="text" name="keyword" id="keyword" required>
						</div>
						
						<div class="row" style="margin-bottom: 5px;">
							<label for="category">Category</label>
							<select id="category" size=1 name="category">
								<option value="default">default</option>
								<option value="cafe">cafe</option>
								<option value="bakery">bakery</option>
								<option value="resturant">resturant</option>
								<option value="beauty_salon">beauty salon</option>
								<option value="casino">casino</option>
								<option value="movie_theater">movie theater</option>
								<option value="lodging">lodging</option>
								<option value="airport">airport</option>
								<option value="train_station">train station</option>
								<option value="subway_station">subway station</option>
								<option value="bus_station">bus station</option>
							</select>
						</div>

						<div class="row" style="margin-bottom: 5px;">
							<div style="display:inline-block;">
								<label for="distance">Distance (miles) </label><input type="text" name="distance" id="distance" placeholder=10> <label>from</label>
								<br/>
								 &nbsp;
							</div>
							<div style="display:inline-block;">
								<input type="radio" name="location" value="here">Here<input type="text" style="visibility: hidden; max-width:100px;" name="here" id="here">
								<br/>
								<input type="radio" name="location" value="entry"><input type="text" id="inputLocation" name="inputLocation" placeholder="location" required>
							</div>
							<div id="placeidContainer" style="visibility: hidden; position: absolute;"></div>
						</div>

						<br/>

						<div class="row" style="margin-bottom: 5px;">
							<div style="width: 50%; text-align: center;">
								<input type="submit" value="Search" id="search" disabled="true">
								<input type="button" value="Clear" id="clear">
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="row" id="search_result" style="margin-top: 25px; margin-bottom: 80px; display: none;">
				<div class="row tcenter"><b id="place_name"></b></div>
				<div class="row tcenter topmargin" style="margin-bottom:5px;"><span id="reviewButton">click to <span id="reviews_text">show</span> reviews<br><img src="arrow_down.png" style="width: 30px; height: auto;" id="reviews_arrow"/></span></div>
				<div class="row" id="reviews" style="display: none;"></div>

				<!-- Unnecessary div to create space at end as per video -->
				<div class="row" style="min-height: 150px;">
					<div class="row tcenter topmargin" style="margin-bottom:5px;"><span id="photoButton">click to <span id="photos_text">show</span> photos<br><img src="arrow_down.png" style="width: 30px; height: auto;" id="photos_arrow"/></span></div>
					<div class="row" id="photos" style="display: none;"></div>
				</div>
			</div>
		</form>

		<script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=Map_Key"></script>
		
		<script type="text/javascript">
			// General javascript
			(function(){
				//Initializing the values
				document.getElementById("keyword").value = "<?php echo $keyword;?>";
				let def_cat = "<?php echo $category;?>";
				let cat_obj = document.getElementById("category");
				for(let i=0; i<cat_obj.options.length; i++){
					if(cat_obj.options[i].value==def_cat){
						cat_obj.options[i].selected = true;
						break;
					}	
				}
				document.getElementById("distance").value = "<?php echo $distance;?>";
				let def_loc = "<?php echo $location;?>";
				if(def_loc=="here"){
					document.getElementsByName("location")[0].checked = true;
					document.getElementById("inputLocation").disabled = true;
				} else {
					document.getElementsByName("location")[1].checked = true;
					document.getElementById("inputLocation").disabled = false;
					document.getElementById("inputLocation").value = "<?php echo $inputLocation;?>";
				}

				//Binding the onclick functions
				let locationRadioTrigger = function(){
					if(document.getElementsByName("location")[0].checked){
						document.getElementById("inputLocation").disabled = true;
						document.getElementById("inputLocation").value="";
					} else{
						document.getElementById("inputLocation").disabled = false;
					}
				};

				document.getElementsByName("location")[0].onclick = locationRadioTrigger;
				document.getElementsByName("location")[1].onclick = locationRadioTrigger;

				//Call to the http://ip-api.com/json
				let xmlhttp=new XMLHttpRequest();
				xmlhttp.open("GET","http://ip-api.com/json",false); //open, send, responseText are
				xmlhttp.send(); //properties of XMLHTTPRequest

				document.getElementById("here").value = xmlhttp.responseText;

				//Enable the search input button
				document.getElementById("search").disabled=false;

				//Reset Function
				document.getElementById("clear").onclick = function(){
					document.getElementById("keyword").value = "";
					document.getElementById("category").selectedIndex = 0;
					document.getElementById("distance").value = "";
					document.getElementById("inputLocation").value = "";
					document.getElementsByName("location")[0].checked = true
					locationRadioTrigger();
					document.getElementById("search_result").innerHTML = "";
				};				

				let result_space = document.getElementById("search_result");

				// Check whether the page is in the state to display the search results
				let page_state = <?php echo $page_state; ?>;

				if(page_state==1){
					// Code only to be present if the result table is present
					let result_table = <?php echo json_encode($table)?>;

					// Store the lat and lng values
					let origin_lat = <?php echo $lat==null?0:$lat; ?>;
					let origin_lng = <?php echo $lng; ?>;
					result_space.style.display = "block";

					//Table generator
					if(result_table.length>0){
						$content = "<table><thead><tr style='text-align:center;'><th style='width: 90px;'>Category</th><th>Name</th><th>Address</th></tr></thead><tbody>";
						for(index in result_table){
							$content += "<tr><td><img src='" + result_table[index].icon + "' style='max-width: 50px; height: auto;'/></td>";
							$content += "<td style='padding: 0px 10px;'><a class='place'>" + result_table[index].name + "</a></td>";
							$content += "<td style='padding: 0px 10px;'><a class='map'>" + result_table[index].vicinity + "</a><div class='potential_map'></div><div class='direction_options'><div class='direc_item walk'>Walk there</div><div class='direc_item bike'>Bike there</div><div class='direc_item drive'>Drive there</div></div></td></tr>";
						}
						$content += "</tbody></table>";

						result_space.innerHTML =$content;

						// Get the reference of anchor tags with class place
						let places = document.getElementsByClassName("place");

						// Get the reference of anchor tags with class map
						let maps = document.getElementsByClassName("map");

						// Get the reference of anchor tags with class walk
						let walk = document.getElementsByClassName("walk");

						// Get the reference of anchor tags with class walk
						let bike = document.getElementsByClassName("bike");

						// Get the reference of anchor tags with class walk
						let drive = document.getElementsByClassName("drive");

						//The script file of google maps will be only present when the result table is present
						let directionsService = null;
	  					let directionsDisplay = null;
	  					let marker = null;

						// Add the onclick functionality
						for(index=0; index<places.length; index++){
							//Local reference
							let i = index;

							// Onclick event to be triggered when the link to open the new page with the information of the place
							places[index].onclick = function(){
								document.getElementById("placeidContainer").innerHTML = "<input type='text' name='placeid' value=" + result_table[i].placeid + ">";
								document.getElementById("myForm").submit();
							};

							//Trigger the map box
							maps[index].onclick = function(){
								// Get the reference of the direction and the map container
								let map_ref = document.getElementsByClassName("potential_map")[i];
								let direction_box = document.getElementsByClassName("direction_options")[i];

								// Code to flip the map trigger
								if(!map_ref.classList.contains("map_box")){
									let uluru = {lat: result_table[i].lat, lng: result_table[i].lng};
							        let map = new google.maps.Map(map_ref, {
							          zoom: 13,
							          center: uluru
							        });

							        // Initialize a new direction service object
							        directionsService = new google.maps.DirectionsService();
	  								directionsDisplay = new google.maps.DirectionsRenderer();

							        directionsDisplay.setMap(map);

							        // Code to put the marker
							        marker = new google.maps.Marker({
							          position: uluru,
							          map: map
							        });
							        map_ref.classList.add("map_box");
							        direction_box.classList.add("direction_option_box");
								} else {
									map_ref.innerHTML="";
									map_ref.classList.remove("map_box");

									direction_box.classList.remove("direction_option_box");
								}
							};

							let direction_update = function(mode, start, end){
								var request = {
									origin: start,
									destination: end,
									travelMode: mode
								};
								directionsService.route(request, function(result, status) {
									if (status == 'OK') {
										directionsDisplay.setDirections(result);
									}
								});
								marker.setMap(null);
							}

							walk[index].onclick = function(){
								direction_update('WALKING', {lat: origin_lat, lng: origin_lng}, {lat: result_table[i].lat, lng: result_table[i].lng});
							};

							bike[index].onclick = function(){
								direction_update('BICYCLING', {lat: origin_lat, lng: origin_lng}, {lat: result_table[i].lat, lng: result_table[i].lng});
							};

							drive[index].onclick = function(){
								direction_update('DRIVING', {lat: origin_lat, lng: origin_lng}, {lat: result_table[i].lat, lng: result_table[i].lng});
							};
						}

					} else {
						result_space.innerHTML = "<div class='noresults'>No Records has been found</div>";
					}
				} else if(page_state==2){
					//Stores the size of the image list- 5 or less than 5
					let images_length = <?php echo $images_length;?>;
					let reviews = <?php echo json_encode($reviews);?>;
					let reviewsDOM = document.getElementById("reviews");
					let photosDOM = document.getElementById("photos");
					let name = "<?php echo $name; ?>";
					result_space.style.display = "block";

					// Display/Hide reviews
					let trigger_reviews = function(){
						if(document.getElementById("reviews").style.display=="block"){
							document.getElementById("reviews").style.display = "none";
							document.getElementById("reviews_text").innerHTML = "show";
							document.getElementById("reviews_arrow").src = "arrow_down.png"
						} else {
							document.getElementById("reviews").style.display="block";
							document.getElementById("reviews_text").innerHTML = "hide"
							document.getElementById("reviews_arrow").src = "arrow_up.png"

							if(document.getElementById("photos").style.display=="block"){
								trigger_photos();
							}
						}
					};

					// Display/Hide phots
					let trigger_photos = function(){
						if(document.getElementById("photos").style.display=="block"){
							document.getElementById("photos").style.display = "none";
							document.getElementById("photos_text").innerHTML = "show";
							document.getElementById("photos_arrow").src = "arrow_down.png"
						} else {
							document.getElementById("photos").style.display="block";
							document.getElementById("photos_text").innerHTML = "hide"
							document.getElementById("photos_arrow").src = "arrow_up.png"
							
							if(document.getElementById("reviews").style.display=="block"){
								trigger_reviews();
							}
						}
					};

					document.getElementById("place_name").innerHTML = name;

					// Review text table
					let content = "<table style='width: 750px;'>";
					if(reviews.length>0){
						for(i in reviews){
							content += "<tr><th class='tcenter'><img src='" + reviews[i]["profile_photo_url"] + "' style='width: 30px; height: auto;'> &nbsp;" + reviews[i]["author_name"] + "</th></tr>";
							content += "<td>" + reviews[i]["text"] + "</td></tr>";
						}
					} else {
						content += "<tr><th>No Reviews found</th></tr>";
					}
					content += "</table>";
					reviewsDOM.innerHTML = content;
					document.getElementById("reviewButton").onclick = function(){
						trigger_reviews();
					};

					// Place photo table
					content = "<table style='width: 750px;'>";
					if(<?php echo $images_length; ?>>0){
						for(let i=0; i<<?php echo $images_length; ?>; i++){
							content += "<tr><td class='tcenter' style='padding-top: 15px; padding-bottom: 15px;'><a target='_blank' href='./" + i + ".jpg'><img src='./" + i + ".jpg' style='max-width: 720px;'></a></td></tr>";
						}
					} else {
						content += "<tr><th>No Photos found</th></tr>";	
					}

					content += "</table>"
					photosDOM.innerHTML = content;
					document.getElementById("photoButton").onclick = function(){
						trigger_photos();
					};
				}
			})();
		</script>
	</body>
</html>