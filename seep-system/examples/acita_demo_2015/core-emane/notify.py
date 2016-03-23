#!/usr/bin/python

# Import smtplib for the actual sending function
import smtplib

# Import the email modules we'll need
from email.mime.text import MIMEText

def notify(text, email, smtp):
    # Open a plain text file for reading.  For this example, assume that
    # the text file contains only ASCII characters.
    # Create a text/plain message
    msg = MIMEText(text)

    # me == the sender's email address
    # you == the recipient's email address
    me = email
    you = email
    msg['Subject'] = text 
    msg['From'] = me 
    msg['To'] = you 

    # Send the message via our own SMTP server, but don't include the
    # envelope header.
    s = smtplib.SMTP(smtp)
    s.sendmail(me, [you], msg.as_string())
    s.quit()

if __name__ == "__main__":
    notify('Hello world', 'd.okeeffe@imperial.ac.uk', 'smarthost.cc.ic.ac.uk')
