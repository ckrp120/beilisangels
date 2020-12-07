BTW SAMPLE.ZIP-----------------------------------------------
HAI 1.2
BTW ARITH.LOL-----------------------------------------------
	I HAS A x
	I HAS A y
	I HAS A answer

	VISIBLE "x: "
	GIMMEH x

	VISIBLE "y: "
	GIMMEH y

	answer R SUM OF x AN y
	VISIBLE x "+" y " is " answer

	answer R DIFF OF x AN y
	VISIBLE x "-" y " is " answer

	answer R PRODUKT OF x AN y
	VISIBLE x "*" y " is " answer

	answer R QUOSHUNT OF x AN y
	VISIBLE x "/" y " is " answer

	answer R MOD OF x AN y
	VISIBLE x "%" y " is " answer

	answer R BIGGR OF x AN y
	VISIBLE "max(" x "," y ") is " answer

	answer R SMALLR OF x AN y
	VISIBLE "min(" x "," y ") is " answer


BTW BOOL.LOL-----------------------------------------------
  I HAS A flag ITZ WIN
  I HAS A anotherflag ITZ FAIL

  VISIBLE flag " and " anotherflag " is " BOTH OF flag AN anotherflag
  VISIBLE flag " or " anotherflag " is " EITHER OF flag AN anotherflag
  VISIBLE flag " xor " anotherflag " is " WON OF flag AN anotherflag
  VISIBLE "not " flag " is " NOT flag
  
  I HAS A flag3 ITZ WIN
  I HAS A flag4 ITZ WIN
  
  I HAS A flag5
  flag5 R ALL OF flag AN anotherflag AN flag3 AN flag4
  VISIBLE flag5


BTW COMP.LOL-----------------------------------------------
  I HAS A x
  I HAS A y

  VISIBLE "Gimmeh x "
  GIMMEH x
  VISIBLE "Gimmeh y "
  GIMMEH y
  
  BOTH SAEM x AN y
OBTW
  output will be given to the implicit IT variable
  since it was not given to any variable
TLDR

  VISIBLE x "==" y " is " IT  BTW uses the implicit variable
  VISIBLE x "!=" y " is " DIFFRINT x AN y


BTW IO.LOL-----------------------------------------------
 	I HAS A num1
	I HAS A num2

	num1 R 15

	VISIBLE "Enter value for num2: "

	GIMMEH num2		BTW getting input from user

	VISIBLE num1 " is num1"
	VISIBLE num2 " is num2" 


BTW SAMPLE.LOL-----------------------------------------------
OBTW
This is a sample program in lolcode that computes for the age in years or months.
TLDR

  I HAS A choice

  VISIBLE "1. Age in years"
  VISIBLE "2. Age in months"
  VISIBLE "3. Exit"
  VISIBLE "What do you want to do? "
  GIMMEH choice

  OBTW
    line below is not required
    it is only done here since original lolcode specs
      specify that inputs are always YARN
    for your project however, input is implicitly typecasted
      to NUMBR or NUMBAR if possible.
    you may comment the line below if you will use this program
      for your project
  TLDR
  choice R MAEK choice A NUMBR

  I HAS A year

  IT R choice   BTW IT = choice
  WTF?          BTW WTF? uses IT variable
  OMG 1
    VISIBLE "enter year"
    GIMMEH year

    BTW 2020-year
    VISIBLE DIFF OF 2020 AN year " years old"
    GTFO
  OMG 2
    VISIBLE "enter year"
    GIMMEH year
    
    BTW (2020-year)*12
    VISIBLE PRODUKT OF DIFF OF 2020 AN year AN 12 " months old"
    GTFO
  OMGWTF
    VISIBLE "choice is not 1 or 2"
  OIC

  BTW choice!=1 && choice!=2 && choice!=3
  ALL OF DIFFRINT choice AN 1 AN DIFFRINT choice AN 2 AN DIFFRINT choice AN 3

  O RLY?
    YA RLY
      VISIBLE "invalid input!" BTW if choice is not 1,2,3 then invalid input
    NO WAI
      VISIBLE "goodbye!"
  OIC	


BTW VARIABLES.LOL-----------------------------------------------
	I HAS A NUM ITZ 2		BTW NUM = 2
	I HAS A FLOT ITZ 2.5	BTW FLOT = 2.5
	I HAS A NAME ITZ "erika"	BTW NAME = "erika"

BTW printing values
	VISIBLE NUM
	VISIBLE FLOT
	VISIBLE NAME

BTW assignment statement:
	NUM R 17
	
	VISIBLE "NUM is now " NUM

KTHXBYE