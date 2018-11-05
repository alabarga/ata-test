#!/usr/bin/env python3
#
# Nuritas Bioinformatics coding challenge
# Assume you have the protein sequence attached below. It's composed of 800 amino acids.
# Can you find the length of the shortest subsequence that needs to be replaced in order to have all the amino acids equally represented? That means same numbers of "A", "C", "D" and so on.
#

import string
import random
from collections import Counter
seq = ''.join([random.choice(string.ascii_uppercase) for n in xrange(1040)])

seq="ASQLDRFRVFLGWDNGMMLVYQGNKTYEPWLNCDMASPTLSLVSKKAPKILKAADINTTLQPCLAFFIELLLKGIDNERIPNSGSGGREMGLLAPTYSSEATLVTRENNMMEGVHGFENMQDVEVIKLKLPEGYSDVCHFMFMLAGILYIVYDLQMHMSSERETGKFPNPLSDEEFDNPKVVVTNSFVLLEFTVTGAGARPSEQGQEPHNLGATKGSLAISSKTPEIHKDTNPASAQFEGKHTESDAKGVSNEDVFITKERDGREVEPTIKLSKKSVLNPMNVVYNPMLQISEGALRKHSMNDEITILNNTLINKERSVDLGAVSFVNDLLIDKLGCVSGKLAVQLNQSAPPEILHGKDPLTLFLGNTIALMLSKMQRIRVWEEYIFLNLHLALAWEPLLGNLKTHDSQKKWALCGFLIISRIRNLFESEGPVHGLRFSAMPCNTDTRQIKALERFPYAPEKPQWHGDELESPCRLVVASKLLASHDGVSIGKTIGSWPLPAQRYNAYVAWAANDSSILSARPGFAVKEDRLGHSLAQESGTIVVRNPQYGVRFINYNKDEHREFKREATFYPKTVVTHLGAIEGTLMFEIGDAAFTMLHLEEATDAEVRELYYMDMLDKKSSLGRACERIRRVLAPGDHKANGLESAIVSGQNGYEGRIRGLQTFQSNPLKKGRTHMAFCTTLHPFGGLKLVSSQLLKKELAVGTYGHQRTVLHSAEYSCPTSIPNLEGLMYNLISAQGEVNSDAKCHYAALAYICLQVRSVSMNQTEASDLRNFLETPILANDALASEQLLGSKKAKS"
counts = Counter(seq)
counts
counts.keys()
counts.values()

for l,v in counts.items():
    counts.update({l:-40})

n = sum([i for i in counts.values() if i>0])

match = [(l,v) for l,v in sorted(counts.items()) if v>0]
match = dict(match)


subs =[]
for lens in range(n,len(seq)+1):
    for i in range(len(seq)-lens+1):
        subs.append(seq[i:i+lens])

subs = [seq[i:i+lens] for lens in range(n,len(seq)+1) for i in range(len(seq)-lens+1)]

res={}
for s in subs:
    cc = Counter(s)
    for l,v in cc.items():
        if l in  match.keys():
            cc.update({l:-match[l]})

    if all([x>=0 for x in cc.values()]):
        res[s]=len(s)
        print("%s (%s)") % (s, len(s))   
        Counter(s)
        break     


