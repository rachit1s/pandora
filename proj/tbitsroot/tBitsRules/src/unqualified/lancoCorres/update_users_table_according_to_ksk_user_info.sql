update users
set firm_code=k.firm, location=k.location, designation=k.designation, firm_address=k.address, sex=k.sex, full_firm_name=k.full_firm_name
from ksk_user_info k
where k.user_id=users.user_id

