var

    a, b:longint;

    t:text;

Begin

  assign(t,'input.txt');

  reset(t);

  read(t,a);
  read(t,b);

  close(t);



  assign (t,'output.txt');

  rewrite(t);

  write(t,(a+b));

  close(t);

End.