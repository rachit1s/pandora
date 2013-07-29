var counter = 0 ;

function getCounter(){
	return	counter++ ;
}

function getData(){
	return getCounter() + "" ;
}
