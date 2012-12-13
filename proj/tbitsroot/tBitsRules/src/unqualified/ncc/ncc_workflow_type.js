<script type='text/javascript'>
	var nccWorkFlowType = document.getElementById("WorkFlowType");
	var nccArea = document.getElementById("Area");
	var nccEngineeringType = document.getElementById("EngineeringType");
	var nccDisciplineId = document.getElementById("category_id");
	var nccSeverityId = document.getElementById("severity_id");
	
	setWorkflowType();
	nccArea.onchange = function(){setWorkflowType();}
	nccEngineeringType.onchange = function(){setWorkflowType();}
	nccDisciplineId.onchange = function(){setWorkflowType();}
	nccSeverityId.onchange = function(){setWorkflowType();}
	
	function setWorkflowType(){
		nccWorkFlowType.value = getWorkflowType();
	}	
	
	function getWorkflowType(){
		var engineeringType = nccEngineeringType.value;
		var disciplineType = nccDisciplineId.value;
		var severityType = nccSeverityId.value;
		var areaType = nccArea.value;
		
		var BE = 'BasicEngineering';
		var DE = 'DetailedEngineering';
		var MECH = 'Mechanical';
		var ELEC = 'Electrical';
		var CIVIL = 'Civil';
		var CNI = 'CnI';
		var PROCESS = 'Process';
		var BOP = 'BOP';
		var BTG = 'BTG';
		
		//WF-1, Area-BTG, Discipline-Civil, Engg Type-Basic or Detailed Engg,Originating Agency-CSEPDI
		if((areaType == BTG) 
				&& ((engineeringType == BE) || (engineeringType == DE))
				&& (disciplineType == CIVIL))
			return 'WF1';
		//Area-BTG, Discipline-Mechanical or Electrical or C&I, Engg Type-Basic or Detailed Engg, Originating Agency-CSEPDI
		else if((areaType == BTG) 
			&& ((engineeringType == BE) || (engineeringType == DE))
			&& ((disciplineType == MECH) || (disciplineType == ELEC) || (disciplineType == CNI)))
			return 'WF2';
		//WF-3, Area-BOP, Discipline-Civil,Engg Type-Basic or Detailed Engg,Originating Agency-EDTD
		//WF-4, rea-BOP, Discipline-Civil,Package:Chimney,Engg Type-Basic or Detailed Engg,Originating Agency-STUP
		else if((areaType == BOP) 
			&& ((engineeringType == BE) || (engineeringType == DE))
			&& (disciplineType == CIVIL)){
			if (severityType == 'Chimney')
				return 'WF4';
			else
				return 'WF3'
		}
		//WF-5,Area-BOP,Discipline-Mechanical or electrical or C&I,	Engg Type-Basic Engg,Originating Agency-DCPL
		else if((areaType == BOP) 
			&&  (engineeringType == BE)
			&&  ((disciplineType == MECH) || (disciplineType == ELEC) || (disciplineType == CNI)))
			return 'WF5';
		//WF-6,	Area-BOP, Discipline-Mechanical or electrical or C&I, Engg Type-Detail Engg, Originating Agency-VENDORS
		else if((areaType == BOP) 
			&&  (engineeringType == DE)
			&&  ((disciplineType == MECH) || (disciplineType == ELEC) || (disciplineType == CNI)))
			return 'WF6';
		//WF-7, Area-BOP, Discipline-Process, Engg Type-Basic or Detail Engg, Originating Agency-OTHERS
		else if((areaType == BOP) 
			&& ((engineeringType == BE) || (engineeringType == DE))
			&& (disciplineType == PROCESS))
			return 'WF7';	
	}
</script>