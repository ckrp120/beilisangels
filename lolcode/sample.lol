OBTW
This is a sample program in lolcode that computes for the age in years or months.
TLDR

HAI 1.2
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

KTHXBYE
