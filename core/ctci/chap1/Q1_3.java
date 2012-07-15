public class Q1_3
{
public static void removeDuplicates(char[] str) {
if (str == null) return;
int len = str.length;
if (len < 2) return;

int tail = 1;

for (int i = 1; i < len; ++i) {
int j;
for (j = 0; j < tail; ++j) {
if (str[i] == str[j]) break;
}
if (j == tail) {
str[tail] = str[i];
++tail;
}
}
System.out.println("The output of method : " + new String(str));
}

public static void main(String argv[])
{
  if( argv.length == 0 ) 
  {
    System.out.println("ERROR : you did not pass the string");
    return;
  }
  char[] str = argv[0].toCharArray();
  removeDuplicates(str);
  System.out.println("after removing : " + new String(str));

  return ;
}
}
