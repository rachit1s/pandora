

/*
*/

#include <iostream>
#include <cmath>
using namespace std;

double Sqroot(double x)
{
    double fa = x / 2;
    double sa = x / fa;
    double ta = (fa + sa) / 2.0;
    cout << "fa = " << fa << "\t sa = " << sa << "\t ta = " << ta << endl;
    while (abs(x - pow(ta, 2)) > 0.000000001)
    {
        fa = ta;
        sa = x / fa;
        ta = (fa + sa) / 2;
        cout << "fa = " << fa << "\t sa = " << sa << "\t ta = " << ta << endl;
    }
    return ta;
}

int main()
{
	double d= 10 ;
	double sq = sqrt(d);
	double mysq = Sqroot(d);

	cout << "sq = " << sq << "\tmysq = " << mysq << endl;
}