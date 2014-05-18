#!/usr/bin/env python

import cgi
import os
import sys

from Tkinter import Tk
from tkFileDialog import askopenfilename, asksaveasfile

WIDTH = 960
HEIGHT = 540

class Opname(object):
    def __init__(self):
        self.titel = ''
        self.omschrijving = ''
        self.onderdelen = []
    def html(self):
        jqsrc = (
                "http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/"
                "jquery.min.js")
        string = (
                "<!DOCTYPE html>\n"
                "<html>\n\n"
                "<script type='text/javascript' src='{jquerysrc}'></script>\n\n"
                "<link href='../vidplayer/vidstyle.css' rel='stylesheet'>\n"
                "<link href='http://vjs.zencdn.net/4.0/video-js.css' "
                "rel='stylesheet'>\n"
                "<script src='http://vjs.zencdn.net/4.0/video.js'></script>\n"
                "<script src='../vidplayer/playlist.min.js'></script>\n\n"
                "<title> {paginatitel} </title>\n\n"
                "<div class='wrapper'>\n"
                "\t<div class='vidpage_header'>\n"
                "\t\t<h1> {paginatitel}</h1>\n"
                "\t</div>\n"
                "\t<div class='content'>\n"
                "\t\t<div id='kijkVideo' style='float: none;'>\n"
                "\t\t\t<video id='vidContainer' class='video-js "
                "vjs-default-skin' preload='none' data-setup='{{}}' controls "
                "width='{videowidth}' height='{videoheight}'>\n"
                "\t\t\t</video>\n"
                "\t\t</div>\n"
                "\t\t<div id='omschrijving' style='float: none;'>\n"
                "\t\t\t{omschrijving}\n"
                "\t\t</div>\n"
                "\t\t<ol id='videoPlaylist' style='float: none;'>\n"
                "{onderdelentext}\n"
                "\t\t</ol>\n"
                "\t</div>\n"
                "\t<div id='vidpage_footer'>\n"
                "\t</div>\n"
                "</div>\n".format(jquerysrc=jqsrc, paginatitel=self.titel,
                    videowidth=WIDTH, videoheight=HEIGHT, 
                    omschrijving='<br \>\n\t\t\t'.join(self.omschrijving.split('\n')),
                    onderdelentext='\n'.join(['\n'.join(['\t\t\t%s' % a for a in
                        x.html().split('\n')]) for x in
                        self.onderdelen])))
        return string

class Onderdeel(object):
    def __init__(self):
        self.origineel = ''
        self.bestandsnaam = ''
        self.titel = ''
        self.subonderdelen = []
        self.duur = 0
    def html(self):
        string = (
                "<li class='vid_element' id='{bestandsnaam}'>\n"
                "\t<span style='float: right;'> {duurstring} </span> "
                "{onderdeeltitel}\n"
                "</li>\n"
                "<ol class='subonderdelen' style='float: none'>\n"
                "{subonderdelentext}\n"
                "</ol>".format(bestandsnaam=self.bestandsnaam,
                    duurstring=sec_to_str(self.duur), onderdeeltitel=self.titel,
                    subonderdelentext='\n'.join([x.html() for x in
                        self.subonderdelen])))
        return string

class SubOnderdeel(object):
    def __init__(self):
        self.titel = ''
        self.starttijd = 0
    def html(self):
        string = (
                "\t<li class='subonderdeel' id='{secstart}'>\n"
                "\t\t<span style='float: right;'>{strstart} </span> {subtitel}\n"
                "\t</li>".format(secstart=self.starttijd,
                    strstart=sec_to_str(self.starttijd), subtitel=self.titel))
        return string

def time_to_sec(tijdstring):
    """
        Converteer een tijdstring HH:MM:SS of MM:SS naar een totaal aantal
        seconden.
    """
    parts = tijdstring.split(':')
    if len(parts) == 2:
        total = 60*int(parts[0]) + int(parts[1])
    elif len(parts) == 3:
        total = 3600*int(parts[0]) + 60*int(parts[1]) + int(parts[2])
    else:
        print("Onbekende tijdstring %s gelezen. Weet niet wat ik hier mee moet",
                tijdstring)
    return total

def sec_to_str(seconden):
    """
        Converteer een tijd in seconden (int) naar een tijdstring in formaat
        HH:MM:SS.
    """
    hours = seconden//3600
    minutes = (seconden%3600)//60
    seconds = seconden%60
    return '%02i:%02i:%02i' % (hours, minutes, seconds)

def s2h(string):
    """
        Converteer string naar correcte HTML characters. Dit vervangt niet-HTML
        tekens naar de juiste karakter codes.
    """
    return cgi.escape(string).encode('ascii', 'xmlcharrefreplace')

def parse_overzichtsbestand(bestand):
    """
        Converteer een opname overzicht naar een Opname instance.
    """
    fid = open(bestand, 'r')
    lines = fid.readlines()
    blocks = []
    block = []
    for line in lines:
        if line.strip() == '':
            if block:
                blocks.append(block)
            block = []
        else:
            block.append(line.rstrip())
    opname = Opname()
    opname.titel = s2h(blocks[0][0])
    opname.omschrijving = '\n'.join([s2h(x) for x in blocks[0][1:]])
    for block in blocks[1:]:
        onderdeel = Onderdeel()
        onderdeel.origineel = block[0]
        onderdeel.bestandsnaam = '.'.join(block[1].split('.')[:-1])
        onderdeel.titel = s2h(block[2])
        for line in block[3:-1]:
            if line.startswith('\t'):
                sub = SubOnderdeel()
                try:
                    (tijd, titel) = line.split(' ', 1)
                except ValueError:
                    print("Fout bij regel: %s. Dit moet worden gecorrigeerd.\n" %
                            line)
                    raise SystemExit
                sub.titel = s2h(titel)
                sub.starttijd = time_to_sec(tijd)
                onderdeel.subonderdelen.append(sub)
        onderdeel.duur = time_to_sec(block[-1].strip())
        opname.onderdelen.append(onderdeel)
    return opname

def guimain():
    Tk().withdraw()
    filename = askopenfilename(filetypes=[('text bestanden', '.txt'),
        ('alle bestanden', '.*')])
    opname = parse_overzichtsbestand(filename)
    name = os.extsep.join(filename.split(os.extsep)[:-1])
    name += os.extsep + 'html'
    sid = asksaveasfile(mode="w", defaultextension="html", initialfile=name)
    sid.write(opname.html())
    sid.close()

def main():
    if len(sys.argv) < 2:
        print("Geen inputbestand gegeven. Gebruik: %s bestand" % sys.argv[0])
        raise SystemExit
    elif len(sys.argv) > 3:
        print("Meer dan 1 inputbestand gegeven. Alleen het eerste "
              "bestand zal worden gebruikt.")
    opname = parse_overzichtsbestand(sys.argv[1])
    html = opname.html()
    name = os.extsep.join(sys.argv[1].split(os.extsep)[:-1])
    filename = name + os.extsep + 'html'
    with open(filename, 'w') as fid:
        fid.write(html)
    print("Output geschreven naar: %s" % filename)


if __name__ == '__main__':
    guimain()
