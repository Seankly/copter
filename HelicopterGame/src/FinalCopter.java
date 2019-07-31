import java.applet.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.Timer;

    public class  FinalCopter extends Applet implements KeyListener, ActionListener, Runnable
    {
        private ArrayList<Point> dustList = new ArrayList<Point>(); //list of dust-trails
        private ArrayList<Rectangle> rectList = new ArrayList<Rectangle>(); //list of obstacles
        private int score=0; //time counter and score
        private int highScore = 0; //high score for the session
        private int[] x,y,bx,by;//x and y coordinates for the top and bottom boundary polygons
        private int vY,rY, start; //how varied the Y values can be, how far down they can be, and X values
        private Polygon p,old,bp,bOld; //boundary polygons
        private Timer time; //the timer
        private boolean[] keys; //keyListener array
        private boolean gameStarted; //whether or not you're playing yet.
        private boolean paused; //whether the game is paused.
        private boolean fastMode; //whether the copter is in fast or slow mode.
        private int gameMode; //what difficulty the game is.
  	
        private int v, acc; //velocity and acceleration
        private Rectangle r; //hit-box of copter
        private boolean gameOver; //determines whether the game is over.
        private boolean ted;
   	
        public void init()
        {
           gameOver = false;
            keys = new boolean[120];
            addKeyListener(this);
            vY = 50;
            rY = 50;
            resize(500,500);
            p = new Polygon();
            bp = new Polygon();
            old = new Polygon();
            bOld = new Polygon();
            v = 0;
            acc = 0;
            r = new Rectangle(170,156,40,28);
            start=500;
            gameStarted = false;
            paused = false;
            fastMode = false;
            gameMode = 4;
            time = new Timer((7-gameMode)*10, this);
            ted = false;
        }
  	
        public static void runner(String[] args)
        {}
   	
        public void actionPerformed(ActionEvent e)
        {
            if(gameStarted)
            {
                if(!gameOver)
                {
                    if(!(p.contains(500,1)))
                    {
                        makeNewPolygons();
                    }
                    if(old != null && old.contains(0,0))
                    {
                        old.translate(-10,0);
                        bOld.translate(-10,0);
                    }
               	
                    p.translate(-10,0);
                    bp.translate(-10,0);

                    for(int z = 0; z< rectList.size();z++)
                    {
                        rectList.get(z).translate(-10,0);
                        if((int)(rectList.get(z).getX()) < 0)
                        {
                            rectList.remove(z);
                            z--;
                        }
                    }
                    score++;
                    newRectandDust();
                    for(int z = 0; z<dustList.size();z++)
                    {
                        dustList.get(z).translate(-10,0);
                    }
                    checkConditions();
                }
            }
            controlSpeed();
   
            checkPauseState();
            checkModes();
       	
            repaint();
        }
   	
        public void paint(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g;
            g2.fillRect(0,0,500,500);
       	
            if(!gameStarted && !gameOver && !paused) //draw Start Screen
            {
                drawStartScreen(g2);
            }
            else if(!gameStarted && !gameOver && paused) //draw Pause Screen
            {
                drawPauseScreen(g2);
            }
            else if(gameStarted)
            {
                if(!gameOver)
                {
                    g2.setColor(Color.GREEN);
                    if(ted)
                      g2.setColor(new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
   
                    if(!(p.contains(500,1)))
                    {
                        makeNewPolygons();
                    }
               	
                    if(old != null && old.contains(0,0)) //move and draw the old polygons
                        drawOld(g2);
   
                    drawNew(g2);//move and draw the new polygons
                    drawRects(g2);//move and draw the rectangles, if off screen, delete.
                    drawCopter(g2);
               	
                    drawDust(g2);
                    drawScore(g2);
                }
            }

            checkGameState(g2);
            g2.setColor(Color.MAGENTA);
            if(ted)
                g2.setColor(new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
            if(fastMode)
                g2.drawString("HELI SPEED:  FAST",370,10);
            else
                g2.drawString("HELI SPEED:  SLOW",370,10);
           	
            g2.drawString("GAME SPEED: "+gameMode,10,10);
            g2.drawString("Hold SPACE to fly!",210,490);
        }
   	
        public void drawStartScreen(Graphics g) //draw
        {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);
            g2.drawString("Hit Enter To Begin",200,250);
            g2.drawString("Hit M and N to toggle game-speed",170,260);
            g2.drawString("Hit S to toggle the copter-speed",180,270);
            g2.drawString("Hit P to pause the game",190, 280);
        }
   	
        public void drawPauseScreen(Graphics g) //draw
        {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);
            g2.drawString("PAUSED: HIT SPACE TO RESUME", 180, 250);
            g2.drawString("Press S to toggle Speed",190,260);
        }
   	
        public void makeNewPolygons() //timer
        {
               old = p;
               bOld = bp;
               start=500;
          	
               x = new int[100];
               y = new int[100];
               bx = new int[100];
               by = new int[100];
               x[0] = 500;
               y[0] = -10;
               bx[0] = 500;
               by[0] = 510;
               int aX=50;
               for(int a = 1; a< x.length-1; a++)
               {
                   if(a%20==1)
                       aX=(int)(Math.random()*rY);
                   x[a] = start;
                   bx[a] = start;
                   start+=10;
                   y[a] = (int)(Math.random()*vY)+aX+2;
                   by[a] = 500 - (int)(Math.random()*vY)-rY+aX-2;
               }
               y[y.length-2]=0;
               y[y.length-1]=0;
               bx[bx.length-1]=start;
               by[by.length-1]=500;
               rY+=5;
               p = new Polygon(x,y,x.length);
               bp = new Polygon(bx,by,bx.length);
        }
   	
        public void drawOld(Graphics g) //draw
        {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.GREEN);
            if(ted)
                    g2.setColor(new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
            g2.fill(old);
            g2.fill(bOld);
        }
   	
        public void drawNew(Graphics g) //draw
        {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.GREEN);
            if(ted)
                     g2.setColor(new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
            g2.fill(p);
            g2.fill(bp);
        }
   	
        public void drawRects(Graphics g) //draw
        {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.GREEN);
            if(ted)
                   g2.setColor(new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
            for(int z = 0; z< rectList.size();z++)
            {
                g2.fill(rectList.get(z));
            }
        }
   	
        public void controlSpeed() //timer
        {
            if(!fastMode)
                {
                    if(keys[KeyEvent.VK_SPACE])
                        acc-=2;
                    else
                        acc+=3;
                }
                else
                {
                    if(keys[KeyEvent.VK_SPACE])
                        acc-=4;
                    else
                        acc+=6;
                }
           	
                v += (acc * 3/16);
        }
   	
        public void newRectandDust() //timer
        {
            if(score % 50 == 0)
                rectList.add(new Rectangle(500,(int)(Math.random()*300)+rY,30,80));

             if(score % 5 == 0)
                dustList.add(new Point(140,170+v));
        }
   	
        public void drawDust(Graphics g) //draw
        {
            Graphics2D g2 = (Graphics2D) g;
            for(int z = 0; z<dustList.size();z++)
            {
                int dx = (int)dustList.get(z).getX();
                int dy = (int)dustList.get(z).getY();
                    g2.setColor(Color.GRAY);
                    if(dx < 50)
                        g2.setColor(Color.GRAY.darker());
                if(dx > 50)
                    g2.fillOval(dx+1,dy+1,12,12);
           	
                g2.fillOval(dx,dy,4,4);
                g2.fillOval(dx,dy+6,4,4);
                g2.fillOval(dx,dy+12,4,4);
                g2.fillOval(dx+6,dy,4,4);
                g2.fillOval(dx+6,dy+12,4,4);
                g2.fillOval(dx+12,dy,4,4);
                g2.fillOval(dx+12,dy+6,4,4);
                g2.fillOval(dx+12,dy+12,4,4);
                if(dx < -50)
                {
                    dustList.remove(z);
                    z--;
                }
            }
        }
   	
        public void checkConditions() //timer
        {
             for(int z = 0; z< rectList.size(); z++)
                if(r.intersects(rectList.get(z)))
                    gameOver = true;
            if(p.intersects(r) || bp.intersects(r) || old.intersects(r) || bOld.intersects(r))
                gameOver = true;
            if(r.y >500 || r.y < 0)
                gameOver = true;
            if(gameOver)
            {
                time.stop();
                gameStarted = false;
            }
        }
   	
        public void drawCopter(Graphics g) //draw
        {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);       	
            if(ted)
                  g2.setColor(new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
            g2.drawLine(162, 173 + v, 170, 173 + v);        //back line
            g2.drawLine(178, 179 + v, 178, 184 + v);         //left lander support line
            g2.drawLine(203, 179 + v, 203, 184 + v);       //right lander support line
            g2.drawLine(175, 184 + v, 206, 184 + v);     //lander line
            g2.setColor(Color.BLUE);
            if(ted)
                    g2.setColor(new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
            g2.fillOval(165, 156 + v, 50, 12);               //propeller oval
            g2.fillOval(160, 168 + v, 6, 6);            //back propeller
            g2.drawLine(175, 184 + v, 206, 184 + v);     //lander line
            g2.setColor(Color.WHITE);
            if(ted)
                     g2.setColor(new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
            g2.drawLine(165, 162 + v, 210, 162 + v);       //propeller line
            g2.drawOval(170, 162 + v, 40, 20);       //copter hull
            r.setLocation(170, v + 156);
        }
   	
        public void drawScore(Graphics g) //draw
        {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);
            g2.drawString(""+score,r.x+8,r.y+22);
        }
   	
        public void checkPauseState() //timer
        {
            if(keys[KeyEvent.VK_P] && !paused && gameStarted)
            {
                time.stop();
                gameStarted = false;
                paused = true;
            }
            if(keys[KeyEvent.VK_SPACE] && paused)
            {
                time.start();
                paused = false;
                gameStarted = true;
            }
        }
   	
        public void checkGameState(Graphics g) //draw
        {
            Graphics2D g2 = (Graphics2D)g;
            if(gameOver)
            {
                g2.setColor(Color.WHITE);
                g2.drawString("GAME OVER!",200,250);
                g2.drawString("Score:  "+score,210,270);
                g2.drawString("Hit Enter to Retry!",190,260);
                if(score > highScore)
                    highScore = score;
                g2.drawString("High Score:  " + highScore, 190, 280);
            }
        }
   	
        public void checkModes() // timer
        {
            if(keys[KeyEvent.VK_S])
                fastMode = !fastMode;

            if(keys[KeyEvent.VK_M] && gameMode < 6)
            {
                gameMode++;
                time.stop();
                time = new Timer((7-gameMode)*10,this);
                time.start();
            }
            if(keys[KeyEvent.VK_N] && gameMode > 1)
            {
                gameMode--;
                time.stop();
                time = new Timer((7-gameMode)*10,this);
                time.start();
            }
        	
            if(keys[KeyEvent.VK_1])
            {
                gameMode=1;
                time.stop();
                time = new Timer(60,this);
                time.start();
            }
            if(keys[KeyEvent.VK_2])
            {
                gameMode=2;
                time.stop();
                time = new Timer(50,this);
                time.start();
            }
            if(keys[KeyEvent.VK_3])
            {
                gameMode=3;
                time.stop();
                time = new Timer(40,this);
                time.start();
            }
            if(keys[KeyEvent.VK_4])
            {
                gameMode=4;
                time.stop();
                time = new Timer(30,this);
                time.start();
            }
            if(keys[KeyEvent.VK_5])
            {
                gameMode=5;
                time.stop();
                time = new Timer(20,this);
                time.start();
            }
            if(keys[KeyEvent.VK_6])
            {
                gameMode=6;
                time.stop();
                time = new Timer(10,this);
                time.start();
            }
        }
   	
        public void keyPressed(KeyEvent e)
        {
       	
             if(e.getKeyCode() >= 120)
                e.consume();
             else
             try { keys[e.getKeyCode()] = true; }
             catch (ArrayIndexOutOfBoundsException outbounds)
             {e.consume();}
        }
   	
        public void keyReleased(KeyEvent e)
        {
            keys[e.getKeyCode()] = false;
        }
        public void keyTyped(KeyEvent e)
        {        	
            if(keys[KeyEvent.VK_ENTER] && !gameStarted && !paused)
             {
                 gameStarted = true;
                 gameOver = false;
                 vY = 50;
                rY = 50;
                p = new Polygon();
                bp = new Polygon();
                old = new Polygon();
                bOld = new Polygon();
                v = 0;
                acc = 0;
                r = new Rectangle(170,156,40,28);
                start=500;
                 score = 0;
                 rectList = new ArrayList<Rectangle>();
                 dustList = new ArrayList<Point>();
                 time.start();
            }
       	
            if(e.getKeyChar() == 't')
                ted = !ted;
           	
            checkPauseState();
            checkModes();
        }
   	
            //Double buffering:
        public void start()
        {
            if (!isVisible())
             {
                 setVisible(true);
             }
            requestFocus();
            requestFocusInWindow();
            requestFocus();
      	
            Thread t = new Thread(this);
            t.start();
        }
        public void run()
        {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            while (true)
            {
                repaint();
                try
                { // Stop thread for 20 milliseconds
                Thread.sleep (20); //throws an InterruptedException, so has be be in a try...
                }
                catch (InterruptedException ex){}
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            }
        }

        public void update (Graphics g)
        {
            if (dbImage == null)
            {
                dbImage = createImage (this.getSize().width, this.getSize().height);
                dbg = dbImage.getGraphics ();
            }
            // clear screen in background
            dbg.setColor (getBackground ());
            dbg.fillRect (0, 0, this.getSize().width, this.getSize().height);
            // draw elements in background
            dbg.setColor (getForeground());
            paint (dbg);
            // draw image on the screen
            g.drawImage (dbImage, 0, 0, this);
        }
        private Image dbImage;
        private Graphics dbg;
    //END DoubleBuffering.
    }

 

