#include <iostream>
using namespace std;

template<typename T>
class Manipulator
{
	private:
		T t;
	public:
		Manipulator(T t)
		{
			this->t = t ;
		}
		void manipulate()
		{
			t.f();
		}

		void manipulateMyF()
		{
			t.myF();
		}
};

class HasF
{
	public:
		void f()
		{
			cout << "f() called.\n";
		}

};

class NoF
{
	public:
		void myF()
		{
			cout << "myF() called.\n";
		}
};

int main()
{
	HasF hasf;
	Manipulator<HasF> hasFM (hasf);
	hasFM.manipulate();

	NoF nof;
	Manipulator<NoF> noFM(nof);
	// noFM.manipulate();
	noFM.manipulateMyF();

	// Manipulator<int> mi ;
	// mi.manipulate();

	return 0;
}