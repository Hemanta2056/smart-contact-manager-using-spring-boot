console.log("this is script file");

const toggleSidebar = () => {

	if ($(".sidebar").is(":visible")) {

		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");

	} else {
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");

	}
};


const search = () => {

	//console.log("searching");

	let query = $("#search-input").val();

	if (query == "") {

		$(".search-result").hide();

	} else {

		//search

		console.log(query);

		let url = `http://localhost:8080/search/${query}`;

		fetch(url)
			.then((response) => {

				return response.json();
			})
			.then((data) => {

				//data
				//console.log(data);

				let text = `<div class='list-group'>`;

				data.forEach((contact) => {

					text += `<a href='/user/${contact.cid}/contact' class ="list-group-item list-group-item-action" > ${contact.name} </a> `
				});

				text += `</div>`;

				$(".search-result").html(text);
				$(".search-result").show();

			});




	}


}





//first request to server to create order


const paymentStart = () => {

	console.log("payment started")

	let amount = $("#payment_field").val();
	console.log(amount);


	if(amount=='' || amount==null){

		alert("amount is required !!!")
		return;
	}

//code..
//we will use ajax to send request to server to order-jquery

$.ajax(
	{

		url:'/user/create_order',
		data:JSON.stringify({amount:amount,info:'order_request'}),
		contentType:'application/json',
		type:'POST',
		dataType:'json',
		success:function(response){
			console.log(response)
		},

		error:function(error){
			console.log(error)
			alert("something went wrong !!")
		}

	}
)

}



